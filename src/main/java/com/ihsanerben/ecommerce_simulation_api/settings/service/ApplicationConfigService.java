package com.ihsanerben.ecommerce_simulation_api.settings.service;

import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.settings.repository.ApplicationConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationConfigService {

    private final ApplicationConfigRepository applicationConfigRepository;

    public String getValue(String key) {
        return applicationConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationConfig", "key", key))
                .getConfigValue();
    }

    public int getInteger(String key) {
        String value = getValue(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Application config '%s' must be an integer.".formatted(key), exception);
        }
    }

    public long getLong(String key) {
        String value = getValue(key);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Application config '%s' must be a long.".formatted(key), exception);
        }
    }

    public boolean getBoolean(String key) {
        String value = getValue(key);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalStateException("Application config '%s' must be true or false.".formatted(key));
        }
        return Boolean.parseBoolean(value);
    }
}
