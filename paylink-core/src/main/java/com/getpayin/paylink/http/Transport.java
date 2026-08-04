package com.getpayin.paylink.http;

import java.time.Duration;
import java.util.Map;

/**
 * Sends one request and returns a {@link PaylinkHttpResponse}. Implementations throw
 * on a network failure or timeout (the SDK wraps that in a
 * {@code PaylinkConnectionException}). Provide a custom one to use a proxy-aware or
 * pooled HTTP client, a framework's client (e.g. Spring's), or a mock in tests.
 */
public interface Transport {

    PaylinkHttpResponse send(String method, String url, Map<String, String> headers, byte[] body, Duration timeout) throws Exception;
}
