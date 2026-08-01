package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.repository.ApplicationConfigRepository;
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
}
