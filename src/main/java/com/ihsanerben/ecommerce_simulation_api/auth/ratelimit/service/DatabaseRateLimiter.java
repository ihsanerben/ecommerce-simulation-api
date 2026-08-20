package com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.service;

import com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.entity.RateLimitEntry;
import com.ihsanerben.ecommerce_simulation_api.exception.RateLimitExceededException;
import com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.repository.RateLimitEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.ClientIdentifierHasher.forLog;

@Service
@Slf4j
public class DatabaseRateLimiter {

    private final RateLimitEntryRepository repository;
    private final Clock clock;

    public DatabaseRateLimiter(RateLimitEntryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = RateLimitExceededException.class)
    public void checkAllowed(String scope, String clientId, int maxAttempts, Duration window) {
        validatePolicy(maxAttempts, window);
        repository.lockBucket(scope, clientId);
        Instant now = clock.instant();
        repository.findByScopeAndClientId(scope, clientId).ifPresent(entry -> {
            if (isExpired(entry, now)) {
                repository.delete(entry);
                return;
            }
            if (entry.getBlockedUntil() != null && now.isBefore(entry.getBlockedUntil())) {
                log.warn("event=rate_limit_rejected scope={} clientRef={}", scope, forLog(clientId));
                throw exceeded(retryAfterSeconds(now, entry.getBlockedUntil()));
            }
        });
    }

    @Transactional(noRollbackFor = RateLimitExceededException.class)
    public void recordAttempt(String scope, String clientId, int maxAttempts, Duration window) {
        validatePolicy(maxAttempts, window);
        repository.lockBucket(scope, clientId);
        Instant now = clock.instant();
        RateLimitEntry entry = repository.findByScopeAndClientId(scope, clientId)
                .filter(current -> !isExpired(current, now))
                .orElseGet(() -> newEntry(scope, clientId, now, window));

        entry.setAttemptCount(entry.getAttemptCount() + 1);
        entry.setUpdatedAt(now);
        if (entry.getAttemptCount() >= maxAttempts) {
            entry.setBlockedUntil(entry.getWindowExpiresAt());
        }
        repository.saveAndFlush(entry);

        if (entry.getBlockedUntil() != null) {
            log.warn("event=rate_limit_reached scope={} clientRef={} attempts={}",
                    scope, forLog(clientId), entry.getAttemptCount());
            throw exceeded(retryAfterSeconds(now, entry.getBlockedUntil()));
        }
    }

    @Transactional
    public void reset(String scope, String clientId) {
        repository.lockBucket(scope, clientId);
        repository.deleteByScopeAndClientId(scope, clientId);
    }

    private RateLimitEntry newEntry(String scope, String clientId, Instant now, Duration window) {
        return RateLimitEntry.builder()
                .scope(scope)
                .clientId(clientId)
                .attemptCount(0)
                .windowStartedAt(now)
                .windowExpiresAt(now.plus(window))
                .updatedAt(now)
                .build();
    }

    private boolean isExpired(RateLimitEntry entry, Instant now) {
        return !now.isBefore(entry.getWindowExpiresAt());
    }

    private void validatePolicy(int maxAttempts, Duration window) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Rate limit maxAttempts must be greater than zero.");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate limit window must be greater than zero.");
        }
    }

    private long retryAfterSeconds(Instant now, Instant blockedUntil) {
        long remainingMillis = Duration.between(now, blockedUntil).toMillis();
        return Math.max(1, (remainingMillis + 999) / 1000);
    }

    private RateLimitExceededException exceeded(long retryAfterSeconds) {
        return new RateLimitExceededException(
                "Too many requests. Please try again later.",
                retryAfterSeconds);
    }
}
