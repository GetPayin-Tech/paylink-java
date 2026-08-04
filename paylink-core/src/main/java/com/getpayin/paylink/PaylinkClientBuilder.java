package com.getpayin.paylink;

import com.getpayin.paylink.exception.PaylinkConfigurationException;
import com.getpayin.paylink.http.JdkHttpTransport;
import com.getpayin.paylink.http.Transport;
import com.getpayin.paylink.internal.PaylinkConfig;
import com.getpayin.paylink.internal.Sleeper;
import java.time.Duration;

/** Fluent builder for {@link PaylinkClient}. */
public final class PaylinkClientBuilder {

    private static final String DEFAULT_BASE_URL = "https://pay.getpayin.com";

    private String publicToken;
    private String hashToken;
    private String baseUrl = DEFAULT_BASE_URL;
    private Duration timeout = Duration.ofSeconds(30);
    private int maxRetries = 2;
    private Transport transport;

    /** The integration's public token, sent as {@code token} on every request. */
    public PaylinkClientBuilder publicToken(String value) {
        this.publicToken = value;
        return this;
    }

    /** The secret signing key; server-side only, never expose to a client. */
    public PaylinkClientBuilder hashToken(String value) {
        this.hashToken = value;
        return this;
    }

    /** API base URL; defaults to {@code https://pay.getpayin.com}. */
    public PaylinkClientBuilder baseUrl(String value) {
        this.baseUrl = value;
        return this;
    }

    /** Per-attempt timeout; defaults to 30 seconds. With retries, total wall time can exceed this. */
    public PaylinkClientBuilder timeout(Duration value) {
        this.timeout = value;
        return this;
    }

    /** How many times to RETRY a failed replay-safe request; defaults to 2 (0 disables). */
    public PaylinkClientBuilder maxRetries(int value) {
        this.maxRetries = value;
        return this;
    }

    /** Custom transport; defaults to a {@link JdkHttpTransport}. */
    public PaylinkClientBuilder transport(Transport value) {
        this.transport = value;
        return this;
    }

    public PaylinkClient build() {
        String resolvedPublicToken = requireNonEmpty(publicToken, "publicToken");
        String resolvedHashToken = requireNonEmpty(hashToken, "hashToken");

        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new PaylinkConfigurationException("timeout must be a positive duration.");
        }
        if (maxRetries < 0) {
            throw new PaylinkConfigurationException("maxRetries must be a non-negative integer.");
        }

        String resolvedBaseUrl = (baseUrl == null || baseUrl.isBlank())
                ? DEFAULT_BASE_URL
                : baseUrl.replaceAll("/+$", "");
        Transport resolvedTransport = transport != null ? transport : new JdkHttpTransport();
        Sleeper sleeper = duration -> Thread.sleep(duration.toMillis());

        PaylinkConfig config = new PaylinkConfig(
                resolvedPublicToken,
                resolvedHashToken,
                resolvedBaseUrl,
                timeout,
                maxRetries,
                resolvedTransport,
                Duration.ofMillis(250),
                sleeper,
                Math::random);

        return new DefaultPaylinkClient(config);
    }

    private static String requireNonEmpty(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new PaylinkConfigurationException(name + " is required and must be a non-empty string.");
        }
        return value;
    }
}
