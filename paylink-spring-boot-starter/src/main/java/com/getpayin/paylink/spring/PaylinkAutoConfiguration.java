package com.getpayin.paylink.spring;

import com.getpayin.paylink.PaylinkClient;
import com.getpayin.paylink.PaylinkClientBuilder;
import com.getpayin.paylink.http.Transport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Registers a {@link PaylinkClient} bean built from {@code paylink.*} properties, so a
 * Spring application can inject the client without wiring tokens by hand. A user-defined
 * {@code Transport} bean (for example one backed by Spring's HTTP client) is used when
 * present; otherwise the SDK's JDK transport is used. Define your own {@code PaylinkClient}
 * bean to override this entirely.
 */
@AutoConfiguration
@EnableConfigurationProperties(PaylinkProperties.class)
public class PaylinkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "paylink", name = "hash-token")
    public PaylinkClient paylinkClient(PaylinkProperties properties, ObjectProvider<Transport> transportProvider) {
        PaylinkClientBuilder builder = PaylinkClient.builder()
                .publicToken(properties.getPublicToken())
                .hashToken(properties.getHashToken());

        if (properties.getBaseUrl() != null) {
            builder.baseUrl(properties.getBaseUrl());
        }
        if (properties.getTimeout() != null) {
            builder.timeout(properties.getTimeout());
        }
        if (properties.getMaxRetries() != null) {
            builder.maxRetries(properties.getMaxRetries());
        }

        Transport transport = transportProvider.getIfAvailable();
        if (transport != null) {
            builder.transport(transport);
        }

        return builder.build();
    }
}
