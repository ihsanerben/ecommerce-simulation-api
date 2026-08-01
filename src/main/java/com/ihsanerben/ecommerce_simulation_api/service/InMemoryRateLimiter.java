package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryRateLimiter {

    private final ConcurrentHashMap<String, AttemptWindow> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryRateLimiter() {
        this(Clock.systemUTC());
    }

    InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void checkAllowed(String scope, String clientId, int maxAttempts, Duration window) {
        validatePolicy(maxAttempts, window);
        Instant now = clock.instant();
        AtomicLong retryAfter = new AtomicLong();
        windows.computeIfPresent(bucketKey(scope, clientId), (key, current) -> {
            if (current.isExpired(now, window)) {
                return null;
            }
            if (current.attempts() >= maxAttempts) {
                retryAfter.set(current.retryAfterSeconds(now, window));
            }
            return current;
        });
        if (retryAfter.get() > 0) {
            throw exceeded(retryAfter.get());
        }
    }

    public void recordAttempt(String scope, String clientId, int maxAttempts, Duration window) {
        validatePolicy(maxAttempts, window);
        Instant now = clock.instant();
        AtomicLong retryAfter = new AtomicLong();
        windows.compute(bucketKey(scope, clientId), (key, current) -> {
            AttemptWindow updated = current == null || current.isExpired(now, window)
                    ? new AttemptWindow(now, 1)
                    : current.incremented();
            if (updated.attempts() >= maxAttempts) {
                retryAfter.set(updated.retryAfterSeconds(now, window));
            }
            return updated;
        });
        if (retryAfter.get() > 0) {
            throw exceeded(retryAfter.get());
        }
    }

    public void reset(String scope, String clientId) {
        windows.remove(bucketKey(scope, clientId));
    }

    private void validatePolicy(int maxAttempts, Duration window) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Rate limit maxAttempts must be greater than zero.");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate limit window must be greater than zero.");
        }
    }

    private String bucketKey(String scope, String clientId) {
        return scope + ":" + clientId;
    }

    private RateLimitExceededException exceeded(long retryAfterSeconds) {
        return new RateLimitExceededException(
                "Too many requests. Please try again later.",
                retryAfterSeconds);
    }

    private record AttemptWindow(Instant startedAt, int attempts) {

        private AttemptWindow incremented() {
            return new AttemptWindow(startedAt, attempts + 1);
        }

        private boolean isExpired(Instant now, Duration window) {
            return !now.isBefore(startedAt.plus(window));
        }

        private long retryAfterSeconds(Instant now, Duration window) {
            long remainingMillis = Duration.between(now, startedAt.plus(window)).toMillis();
            return Math.max(1, (remainingMillis + 999) / 1000);
        }
    }
}
