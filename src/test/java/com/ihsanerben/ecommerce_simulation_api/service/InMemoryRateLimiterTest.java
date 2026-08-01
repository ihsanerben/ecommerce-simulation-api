package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryRateLimiterTest {

    private MutableClock clock;
    private InMemoryRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-01T12:00:00Z"));
        rateLimiter = new InMemoryRateLimiter(clock);
    }

    @Test
    void recordAttempt_whenLimitIsReached_throwsWithRetryAfter() {
        Duration window = Duration.ofSeconds(60);

        for (int attempt = 1; attempt < 5; attempt++) {
            assertThatCode(() -> rateLimiter.recordAttempt("login", "127.0.0.1", 5, window))
                    .doesNotThrowAnyException();
        }

        assertThatThrownBy(() -> rateLimiter.recordAttempt("login", "127.0.0.1", 5, window))
                .isInstanceOfSatisfying(RateLimitExceededException.class,
                        exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(60));
        assertThatThrownBy(() -> rateLimiter.checkAllowed("login", "127.0.0.1", 5, window))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void checkAllowed_whenWindowExpires_allowsClientAgain() {
        Duration window = Duration.ofSeconds(60);
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                rateLimiter.recordAttempt("login", "127.0.0.1", 5, window);
            } catch (RateLimitExceededException ignored) {
                // Reaching the configured limit is expected in this setup.
            }
        }

        clock.advance(Duration.ofSeconds(60));

        assertThatCode(() -> rateLimiter.checkAllowed("login", "127.0.0.1", 5, window))
                .doesNotThrowAnyException();
    }

    @Test
    void reset_removesAttemptsOnlyForSelectedClientAndScope() {
        Duration window = Duration.ofSeconds(60);
        for (int attempt = 0; attempt < 4; attempt++) {
            rateLimiter.recordAttempt("login", "127.0.0.1", 5, window);
            rateLimiter.recordAttempt("login", "127.0.0.2", 5, window);
        }

        rateLimiter.reset("login", "127.0.0.1");

        assertThatCode(() -> rateLimiter.recordAttempt("login", "127.0.0.1", 5, window))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> rateLimiter.recordAttempt("login", "127.0.0.2", 5, window))
                .isInstanceOf(RateLimitExceededException.class);
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
