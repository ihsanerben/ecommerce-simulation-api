package com.ihsanerben.ecommerce_simulation_api.repository;

import com.ihsanerben.ecommerce_simulation_api.entity.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, String> {
}
