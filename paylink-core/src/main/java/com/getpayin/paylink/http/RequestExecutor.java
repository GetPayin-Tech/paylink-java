package com.getpayin.paylink.http;

import com.getpayin.paylink.exception.PaylinkApiException;
import com.getpayin.paylink.exception.PaylinkConnectionException;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.Version;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runs a request and returns the {@code data} element of the success envelope. Retries
 * transient failures (429, 5xx, connection errors, timeouts) with exponential backoff and
 * full jitter, honoring {@code Retry-After}. A request is only ever replayed when doing so
 * cannot double-charge: GETs, requests carrying an {@code Idempotency-Key}, and calls
 * explicitly flagged replay-safe.
 */
public final class RequestExecutor {

    private static final Duration MAX_DELAY = Duration.ofSeconds(8);

    /** Sent on every request so server-side logs can attribute traffic to a version. */
    public static final String USER_AGENT = "paylink-java/" + Version.VALUE + " java/" + System.getProperty("java.version");

    private RequestExecutor() {
    }

    public static JsonElement execute(PaylinkConfig config, RequestOptions options) {
        boolean replayable = options.replaySafe() != null
                ? options.replaySafe()
                : ("GET".equals(options.method()) || options.idempotencyKey() != null);
        int maxAttempts = replayable ? config.maxRetries() + 1 : 1;

        int attempt = 1;
        while (true) {
            try {
                return attemptRequest(config, options);
            } catch (PaylinkApiException | PaylinkConnectionException error) {
                if (attempt >= maxAttempts || !isTransient(error)) {
                    throw error;
                }
                delay(config, backoff(config, error, attempt), options.path());
                attempt++;
            }
        }
    }

    private static boolean isTransient(RuntimeException error) {
        if (error instanceof PaylinkConnectionException) {
            return true;
        }
        if (error instanceof PaylinkApiException api) {
            return api.status() == 429 || api.status() >= 500;
        }
        return false;
    }

    private static Duration backoff(PaylinkConfig config, RuntimeException error, int attempt) {
        if (error instanceof PaylinkApiException api && api.retryAfter() != null) {
            return min(api.retryAfter(), MAX_DELAY);
        }
        double ceilingSeconds = Math.min(
                config.retryBaseDelay().toMillis() / 1000.0 * Math.pow(2, attempt - 1),
                MAX_DELAY.getSeconds());
        double seconds = config.rng().getAsDouble() * ceilingSeconds;
        return Duration.ofMillis((long) (seconds * 1000));
    }

    private static Duration min(Duration a, Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static void delay(PaylinkConfig config, Duration duration, String path) {
        try {
            config.sleeper().sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaylinkConnectionException("Retry wait for " + path + " was interrupted.", e);
        }
    }

    private static JsonElement attemptRequest(PaylinkConfig config, RequestOptions options) {
        String url = buildUrl(config.baseUrl(), options.path(), options.query());

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        headers.put("User-Agent", USER_AGENT);

        byte[] body = null;
        if (!"GET".equals(options.method())) {
            headers.put("Content-Type", "application/json");
            Map<String, String> payload = options.body() != null ? options.body() : Map.of();
            body = Json.serialize(payload).getBytes(StandardCharsets.UTF_8);
        }
        if (options.idempotencyKey() != null) {
            headers.put("Idempotency-Key", options.idempotencyKey());
        }

        PaylinkHttpResponse response;
        try {
            response = config.transport().send(options.method(), url, headers, body, config.timeout());
        } catch (Exception error) {
            throw new PaylinkConnectionException("Request to " + options.path() + " failed.", error);
        }

        return handleResponse(response);
    }

    private static JsonElement handleResponse(PaylinkHttpResponse response) {
        JsonElement root = parseOrNull(response.body());
        boolean ok = response.status() >= 200 && response.status() < 300;

        if (!ok || isFailureEnvelope(root)) {
            throw toApiError(response.status(), root, response.body(), parseRetryAfter(response.header("Retry-After")));
        }

        if (root != null && root.isJsonObject() && root.getAsJsonObject().has("data")) {
            return root.getAsJsonObject().get("data");
        }

        return root != null ? root : JsonNull.INSTANCE;
    }

    private static JsonElement parseOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Json.parse(text);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private static boolean isFailureEnvelope(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return false;
        }
        JsonObject object = root.getAsJsonObject();
        return object.has("success")
                && object.get("success").isJsonPrimitive()
                && object.get("success").getAsJsonPrimitive().isBoolean()
                && !object.get("success").getAsBoolean();
    }

    @SuppressWarnings("unchecked")
    private static PaylinkApiException toApiError(int status, JsonElement root, String rawText, Duration retryAfter) {
        String message = null;
        Map<String, Object> errors = null;
        Object raw = rawText;

        if (root != null && root.isJsonObject()) {
            message = Json.strOrNull(root, "message");
            if (message == null) {
                message = Json.strOrNull(Json.child(root, "data"), "message");
            }
            JsonElement errorsElement = Json.child(root, "errors");
            Object errorsPlain = errorsElement != null ? Json.toPlain(errorsElement) : null;
            if (errorsPlain instanceof Map) {
                errors = (Map<String, Object>) errorsPlain;
            }
            raw = Json.toPlain(root);
        }

        if (message == null) {
            message = "PayLink API request failed with status " + status + ".";
        }

        return new PaylinkApiException(message, status, errors, raw, retryAfter);
    }

    private static Duration parseRetryAfter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double seconds = Double.parseDouble(value.trim());
            return Duration.ofMillis((long) (Math.max(0, seconds) * 1000));
        } catch (NumberFormatException ignored) {
            return parseRetryAfterDate(value);
        }
    }

    private static Duration parseRetryAfterDate(String value) {
        try {
            ZonedDateTime when = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
            Duration delta = Duration.between(ZonedDateTime.now(when.getZone()), when);
            return delta.isNegative() ? Duration.ZERO : delta;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String buildUrl(String baseUrl, String path, Map<String, String> query) {
        String url = baseUrl + path;
        if (query == null || query.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        boolean first = true;
        for (Map.Entry<String, String> entry : query.entrySet()) {
            builder.append(first ? '?' : '&');
            first = false;
            builder.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return builder.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
