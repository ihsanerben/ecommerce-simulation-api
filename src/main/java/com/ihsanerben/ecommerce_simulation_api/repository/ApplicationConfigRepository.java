package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.entity.ApplicationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationConfigRepository extends JpaRepository<ApplicationConfig, Long> {

    Optional<ApplicationConfig> findByConfigKey(String configKey);
}
