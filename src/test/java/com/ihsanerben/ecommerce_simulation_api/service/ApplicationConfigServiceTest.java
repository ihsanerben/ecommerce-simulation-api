package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.entity.ApplicationConfig;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.repository.ApplicationConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigServiceTest {

    @Mock
    private ApplicationConfigRepository applicationConfigRepository;

    private ApplicationConfigService applicationConfigService;

    @BeforeEach
    void setUp() {
        applicationConfigService = new ApplicationConfigService(applicationConfigRepository);
    }

    @Test
    void getValue_whenKeyExists_returnsValue() {
        ApplicationConfig config = ApplicationConfig.builder()
                .configKey("auth.login.max-attempts")
                .configValue("5")
                .build();
        given(applicationConfigRepository.findByConfigKey("auth.login.max-attempts"))
                .willReturn(Optional.of(config));

        String result = applicationConfigService.getValue("auth.login.max-attempts");

        assertThat(result).isEqualTo("5");
    }

    @Test
    void getValue_whenKeyDoesNotExist_throwsResourceNotFoundException() {
        given(applicationConfigRepository.findByConfigKey("missing.key"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationConfigService.getValue("missing.key"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing.key");
    }

    @Test
    void getInteger_whenValueIsNumeric_returnsInteger() {
        ApplicationConfig config = ApplicationConfig.builder()
                .configKey("auth.login.max-attempts")
                .configValue("5")
                .build();
        given(applicationConfigRepository.findByConfigKey("auth.login.max-attempts"))
                .willReturn(Optional.of(config));

        int result = applicationConfigService.getInteger("auth.login.max-attempts");

        assertThat(result).isEqualTo(5);
    }

    @Test
    void getInteger_whenValueIsNotNumeric_throwsIllegalStateException() {
        ApplicationConfig config = ApplicationConfig.builder()
                .configKey("auth.login.max-attempts")
                .configValue("invalid")
                .build();
        given(applicationConfigRepository.findByConfigKey("auth.login.max-attempts"))
                .willReturn(Optional.of(config));

        assertThatThrownBy(() -> applicationConfigService.getInteger("auth.login.max-attempts"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be an integer");
    }

    @Test
    void getLong_whenValueIsNumeric_returnsLong() {
        given(applicationConfigRepository.findByConfigKey("jwt.refresh-expiration-ms"))
                .willReturn(Optional.of(ApplicationConfig.builder()
                        .configKey("jwt.refresh-expiration-ms")
                        .configValue("604800000")
                        .build()));

        assertThat(applicationConfigService.getLong("jwt.refresh-expiration-ms"))
                .isEqualTo(604800000L);
    }

    @Test
    void getBoolean_whenValueIsBoolean_returnsBoolean() {
        given(applicationConfigRepository.findByConfigKey("auth.cookie.secure"))
                .willReturn(Optional.of(ApplicationConfig.builder()
                        .configKey("auth.cookie.secure")
                        .configValue("true")
                        .build()));

        assertThat(applicationConfigService.getBoolean("auth.cookie.secure")).isTrue();
    }

    @Test
    void getBoolean_whenValueIsInvalid_throwsIllegalStateException() {
        given(applicationConfigRepository.findByConfigKey("auth.cookie.secure"))
                .willReturn(Optional.of(ApplicationConfig.builder()
                        .configKey("auth.cookie.secure")
                        .configValue("yes")
                        .build()));

        assertThatThrownBy(() -> applicationConfigService.getBoolean("auth.cookie.secure"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be true or false");
    }
}
