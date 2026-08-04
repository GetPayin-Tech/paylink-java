package com.getpayin.paylink.exception;

import java.time.Duration;
import java.util.Map;

/**
 * Thrown when the API responds with a non-success envelope ({@code {"success": false}})
 * or a non-2xx status. Carries the HTTP {@link #status()}, the server-provided
 * per-field {@link #errors()}, and the {@link #raw()} response body for inspection.
 *
 * <p>Use {@link #isIdempotencyConflict()} (409), {@link #isRateLimited()} (429), and
 * {@link #isForbidden()} (403 — e.g. card tokenization or recurring payments not
 * enabled for the account) to branch on the common cases.
 */
public class PaylinkApiException extends PaylinkException {

    private final int status;
    private final Map<String, Object> errors;
    private final transient Object raw;
    private final Duration retryAfter;

    public PaylinkApiException(String message, int status, Map<String, Object> errors, Object raw, Duration retryAfter) {
        super(message);
        this.status = status;
        this.errors = errors;
        this.raw = raw;
        this.retryAfter = retryAfter;
    }

    /** The HTTP status code returned by the API. */
    public int status() {
        return status;
    }

    /** The server-provided validation errors keyed by field, or {@code null}. */
    public Map<String, Object> errors() {
        return errors;
    }

    /** The parsed response body (or the raw text when it was not valid JSON). */
    public Object raw() {
        return raw;
    }

    /**
     * How long the server asked us to wait before retrying, from the {@code Retry-After}
     * header (present on 429s), or {@code null}. The SDK already honors this for
     * replay-safe requests; it is exposed so callers can schedule their own retry of a
     * request the SDK will not replay on its own.
     */
    public Duration retryAfter() {
        return retryAfter;
    }

    /** True when the API rejected the request as a duplicate (HTTP 409). */
    public boolean isIdempotencyConflict() {
        return status == 409;
    }

    /** True when the request was rate limited (HTTP 429). */
    public boolean isRateLimited() {
        return status == 429;
    }

    /** True when the API forbade the request (HTTP 403). */
    public boolean isForbidden() {
        return status == 403;
    }
}
