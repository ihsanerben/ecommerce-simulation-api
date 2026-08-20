package com.ihsanerben.ecommerce_simulation_api.auth.repository;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime cutoff);
    long deleteByUserId(Long userId);
}
