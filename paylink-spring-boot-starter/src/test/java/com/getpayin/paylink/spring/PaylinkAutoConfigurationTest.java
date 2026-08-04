package com.getpayin.paylink.spring;

import static org.assertj.core.api.Assertions.assertThat;

import com.getpayin.paylink.PaylinkClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PaylinkAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaylinkAutoConfiguration.class));

    @Test
    void registersPaylinkClientBeanFromProperties() {
        runner.withPropertyValues("paylink.public-token=pub_x", "paylink.hash-token=secret_y")
                .run(context -> {
                    assertThat(context).hasSingleBean(PaylinkClient.class);
                    assertThat(context.getBean(PaylinkClient.class).invoices()).isNotNull();
                });
    }

    @Test
    void backsOffWhenTokensMissing() {
        runner.run(context -> assertThat(context).doesNotHaveBean(PaylinkClient.class));
    }
}
