package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RevokedAccessTokenCleanupService {
    private final RevokedAccessTokenRepository revokedTokenRepository;

    @Scheduled(fixedDelayString = "${app.auth.revoked-token-cleanup-delay-ms:3600000}")
    @Transactional
    public void deleteExpiredTokens() {
        revokedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
