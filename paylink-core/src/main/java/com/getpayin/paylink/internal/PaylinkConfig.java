package com.getpayin.paylink.internal;

import com.getpayin.paylink.http.Transport;
import java.time.Duration;
import java.util.function.DoubleSupplier;

/**
 * Validated, internal configuration passed to every service. The {@code sleeper} and
 * {@code rng} are injected so retry backoff is deterministic under test.
 */
public record PaylinkConfig(
        String publicToken,
        String hashToken,
        String baseUrl,
        Duration timeout,
        int maxRetries,
        Transport transport,
        Duration retryBaseDelay,
        Sleeper sleeper,
        DoubleSupplier rng) {
}
