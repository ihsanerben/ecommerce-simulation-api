package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.entity.ApplicationConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationConfigRepositoryIT extends AbstractIntegrationTest {

    private static final String TEST_KEY = "test.auth.login.max-attempts";

    @Autowired
    private ApplicationConfigRepository applicationConfigRepository;

    @AfterEach
    void cleanUp() {
        applicationConfigRepository.findByConfigKey(TEST_KEY)
                .ifPresent(applicationConfigRepository::delete);
    }

    @Test
    void shouldFindConfigByKey() {
        applicationConfigRepository.saveAndFlush(ApplicationConfig.builder()
                .configKey(TEST_KEY)
                .configValue("5")
                .build());

        Optional<ApplicationConfig> result = applicationConfigRepository
                .findByConfigKey(TEST_KEY);

        assertThat(result)
                .isPresent()
                .get()
                .extracting(ApplicationConfig::getConfigValue)
                .isEqualTo("5");
    }

    @Test
    void shouldRejectDuplicateConfigKey() {
        applicationConfigRepository.saveAndFlush(ApplicationConfig.builder()
                .configKey(TEST_KEY)
                .configValue("5")
                .build());

        ApplicationConfig duplicate = ApplicationConfig.builder()
                .configKey(TEST_KEY)
                .configValue("10")
                .build();

        assertThatThrownBy(() -> applicationConfigRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
