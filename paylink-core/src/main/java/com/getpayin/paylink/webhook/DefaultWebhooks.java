package com.getpayin.paylink.webhook;

import com.getpayin.paylink.exception.PaylinkException;
import com.getpayin.paylink.exception.PaylinkSignatureException;
import com.getpayin.paylink.internal.Json;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.Signature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default webhook verifier. Recomputes the signature over the concatenated values of an
 * ordered subset of the payload, exactly as the server signs it: {@code success},
 * {@code invoice_id}, {@code invoice_status}, {@code message}, plus (for subscription
 * events only, detected by presence) {@code mandate_id}, {@code external_reference},
 * {@code subscription_status}. The signature travels in the payload body.
 */
public final class DefaultWebhooks implements Webhooks {

    private static final List<String> ALWAYS_SIGNED = List.of("success", "invoice_id", "invoice_status", "message");

    private static final List<String> OPTIONAL_SIGNED = List.of("mandate_id", "external_reference", "subscription_status");

    private final PaylinkConfig config;

    public DefaultWebhooks(PaylinkConfig config) {
        this.config = config;
    }

    @Override
    public WebhookEvent verify(String payload) {
        return verify(payload, null);
    }

    @Override
    public WebhookEvent verify(String payload, String hashToken) {
        JsonObject object = parse(payload);
        String secret = hashToken != null ? hashToken : config.hashToken();

        String provided = Json.strOrNull(object, "signature");
        if (provided == null || provided.isEmpty()) {
            throw new PaylinkSignatureException("Webhook payload is missing a signature.");
        }

        if (!Signature.signaturesEqual(computeSignature(object, secret), provided)) {
            throw new PaylinkSignatureException();
        }

        return toEvent(object);
    }

    @Override
    public boolean isValid(String payload) {
        return isValid(payload, null);
    }

    @Override
    public boolean isValid(String payload, String hashToken) {
        try {
            verify(payload, hashToken);
            return true;
        } catch (PaylinkSignatureException e) {
            return false;
        }
    }

    private String computeSignature(JsonObject object, String hashToken) {
        List<String> values = new ArrayList<>();
        for (String key : ALWAYS_SIGNED) {
            values.add(fieldToWire(object, key));
        }
        for (String key : OPTIONAL_SIGNED) {
            if (object.has(key)) {
                values.add(fieldToWire(object, key));
            }
        }
        return Signature.build(values, hashToken);
    }

    private String fieldToWire(JsonObject object, String name) {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return "";
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? "1" : "0";
        }
        return primitive.getAsString();
    }

    @SuppressWarnings("unchecked")
    private WebhookEvent toEvent(JsonObject object) {
        Object plain = Json.toPlain(object);
        Map<String, Object> raw = plain instanceof Map ? (Map<String, Object>) plain : Map.of();

        return new WebhookEvent(
                Json.strOrNull(object, "event"),
                "1".equals(fieldToWire(object, "success")),
                raw,
                Json.int64OrNull(object, "invoice_id"),
                Json.strOrNull(object, "invoice_status"),
                Json.strOrNull(object, "message"),
                Json.strOrNull(object, "auth_code"),
                Json.strOrNull(object, "mandate_id"),
                Json.strOrNull(object, "external_reference"),
                Json.strOrNull(object, "subscription_status"),
                Json.strOrNull(object, "event_triggered_at"),
                Json.strOrNull(object, "timezone"));
    }

    private JsonObject parse(String payload) {
        try {
            JsonElement element = Json.parse(payload);
            if (!element.isJsonObject()) {
                throw new PaylinkException("Webhook payload must decode to a JSON object.");
            }
            return element.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new PaylinkException("Webhook payload is not valid JSON.", e);
        }
    }
}
