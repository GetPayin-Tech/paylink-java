package com.getpayin.paylink.spring;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code paylink.*} configuration (e.g. {@code paylink.public-token}, {@code paylink.hash-token}). */
@ConfigurationProperties(prefix = "paylink")
public class PaylinkProperties {

    /** The integration's public token, sent as {@code token} on every request. */
    private String publicToken;

    /** The secret signing key; server-side only. */
    private String hashToken;

    /** API base URL; defaults to {@code https://pay.getpayin.com} when unset. */
    private String baseUrl;

    /** Per-attempt timeout; defaults to 30s when unset. */
    private Duration timeout;

    /** How many times to retry a failed replay-safe request; defaults to 2 when unset. */
    private Integer maxRetries;

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public String getHashToken() {
        return hashToken;
    }

    public void setHashToken(String hashToken) {
        this.hashToken = hashToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
