package com.getpayin.paylink.http;

import java.util.Map;

/**
 * One API request. {@code replaySafe} overrides the default replay-safety rule; set it
 * {@code Boolean.TRUE} for a POST that is a pure read (check-status) so it can be retried.
 */
public record RequestOptions(
        String method,
        String path,
        Map<String, String> body,
        Map<String, String> query,
        String idempotencyKey,
        Boolean replaySafe) {

    public static RequestOptions post(String path, Map<String, String> body, String idempotencyKey, Boolean replaySafe) {
        return new RequestOptions("POST", path, body, null, idempotencyKey, replaySafe);
    }

    public static RequestOptions get(String path, Map<String, String> query) {
        return new RequestOptions("GET", path, null, query, null, null);
    }
}
