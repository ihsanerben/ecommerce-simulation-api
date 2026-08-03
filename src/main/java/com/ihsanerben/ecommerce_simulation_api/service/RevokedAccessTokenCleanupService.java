package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevokedAccessTokenCleanupService {
    private final RevokedAccessTokenRepository revokedTokenRepository;

    @Scheduled(fixedDelayString = "${app.auth.revoked-token-cleanup-delay-ms:3600000}")
    @Transactional
    public void deleteExpiredTokens() {
        long deletedCount = revokedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("event=revoked_token_cleanup deletedCount={}", deletedCount);
        }
    }
}
