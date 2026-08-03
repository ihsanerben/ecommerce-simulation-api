package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.repository.RateLimitEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RateLimitEntryCleanupService {

    private final RateLimitEntryRepository repository;

    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-delay-ms:36000000}")
    @Transactional
    public void deleteExpiredEntries() {
        repository.deleteExpiredBefore(Instant.now());
    }
}
