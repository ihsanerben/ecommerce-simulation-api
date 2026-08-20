package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.settings.repository.ApplicationConfigRepository;
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
                "004-seed-login-window-seconds",
                "009-create-audit-events",
                "010-create-low-stock-alerts",
                "011-create-chatbot-interactions",
                "012-seed-public-ui-configs");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'audit_events'",
                Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'low_stock_alerts'",
                Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'chatbot_interactions'",
                Integer.class))
                .isEqualTo(1);
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
        assertThat(applicationConfigRepository.findByConfigKey("ui.primary-color"))
                .isPresent()
                .get()
                .extracting(config -> config.getConfigValue())
                .isEqualTo("#f24391");
    }
}
