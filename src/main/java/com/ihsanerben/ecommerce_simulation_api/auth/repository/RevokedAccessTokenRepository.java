package com.ihsanerben.ecommerce_simulation_api.auth.repository;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, String> {
    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
