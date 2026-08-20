package com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.service;

import com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.repository.RateLimitEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitEntryCleanupService {

    private final RateLimitEntryRepository repository;

    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-delay-ms:36000000}")
    @Transactional
    public void deleteExpiredEntries() {
        int deletedCount = repository.deleteExpiredBefore(Instant.now());
        if (deletedCount > 0) {
            log.info("event=rate_limit_cleanup deletedCount={}", deletedCount);
        }
    }
}
