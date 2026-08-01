package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseMigrationIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationConfigRepository applicationConfigRepository;

    @Test
    void shouldApplyChangelogAndSeedApplicationConfigs() {
        List<String> appliedChangeSets = jdbcTemplate.queryForList(
                "SELECT id FROM databasechangelog ORDER BY orderexecuted",
                String.class);

        assertThat(appliedChangeSets).contains(
                "001-baseline-schema",
                "002-create-application-configs",
                "003-seed-login-max-attempts",
                "004-seed-login-window-seconds");
        assertThat(applicationConfigRepository.findByConfigKey("auth.login.max-attempts"))
                .isPresent()
                .get()
                .extracting(config -> config.getConfigValue())
                .isEqualTo("5");
        assertThat(applicationConfigRepository.findByConfigKey("auth.login.window-seconds"))
                .isPresent()
                .get()
                .extracting(config -> config.getConfigValue())
                .isEqualTo("60");
    }
}
