package com.ihsanerben.ecommerce_simulation_api.repository;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class LiquibaseExistingSchemaIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void createPreLiquibaseSchema() throws SQLException, IOException {
        try (Connection connection = openConnection();
                Statement statement = connection.createStatement()) {
            for (String sql : readBaselineSql().split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    @Test
    void shouldMarkBaselineAsRanAndApplyNewChangesets() throws Exception {
        try (Connection connection = openConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database)) {
                liquibase.update();
            }
        }

        try (Connection connection = openConnection();
                Statement statement = connection.createStatement()) {
            assertThat(querySingleValue(statement,
                    "SELECT exectype FROM databasechangelog WHERE id = '001-baseline-schema'"))
                    .isEqualTo("MARK_RAN");
            assertThat(querySingleValue(statement,
                    "SELECT config_value FROM application_configs WHERE config_key = 'auth.login.max-attempts'"))
                    .isEqualTo("5");
            assertThat(querySingleValue(statement,
                    "SELECT config_value FROM application_configs WHERE config_key = 'auth.login.window-seconds'"))
                    .isEqualTo("60");
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private String readBaselineSql() throws IOException {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/sql/001-baseline-schema.sql")) {
            if (input == null) {
                throw new IllegalStateException("Baseline schema resource could not be found.");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String querySingleValue(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getString(1);
        }
    }
}
