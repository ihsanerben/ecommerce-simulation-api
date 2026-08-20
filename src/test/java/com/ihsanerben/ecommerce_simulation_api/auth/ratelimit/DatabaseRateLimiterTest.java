package com.ihsanerben.ecommerce_simulation_api.auth.ratelimit;

import com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.service.DatabaseRateLimiter;
import com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.entity.RateLimitEntry;
import com.ihsanerben.ecommerce_simulation_api.exception.RateLimitExceededException;
import com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.repository.RateLimitEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseRateLimiterTest {

    private static final Instant NOW = Instant.parse("2026-08-03T12:00:00Z");

    @Mock
    private RateLimitEntryRepository repository;

    private DatabaseRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        rateLimiter = new DatabaseRateLimiter(repository, clock);
    }

    @Test
    void recordAttempt_whenFifthFailureIsRecorded_persistsBlockAndThrows() {
        RateLimitEntry entry = entry(4, NOW.plusSeconds(60), null);
        when(repository.findByScopeAndClientId("login", "127.0.0.1"))
                .thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> rateLimiter.recordAttempt(
                "login", "127.0.0.1", 5, Duration.ofSeconds(60)))
                .isInstanceOfSatisfying(RateLimitExceededException.class,
                        exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(60));

        ArgumentCaptor<RateLimitEntry> captor = ArgumentCaptor.forClass(RateLimitEntry.class);
        verify(repository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getAttemptCount()).isEqualTo(5);
        assertThat(captor.getValue().getBlockedUntil()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void checkAllowed_whenPersistedBlockExists_throwsWithRemainingTime() {
        RateLimitEntry entry = entry(5, NOW.plusSeconds(60), NOW.plusSeconds(45));
        when(repository.findByScopeAndClientId("login", "127.0.0.1"))
                .thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> rateLimiter.checkAllowed(
                "login", "127.0.0.1", 5, Duration.ofSeconds(60)))
                .isInstanceOfSatisfying(RateLimitExceededException.class,
                        exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(45));
    }

    @Test
    void checkAllowed_whenWindowExpired_deletesEntryAndAllowsRequest() {
        RateLimitEntry entry = entry(5, NOW, NOW);
        when(repository.findByScopeAndClientId("login", "127.0.0.1"))
                .thenReturn(Optional.of(entry));

        assertThatCode(() -> rateLimiter.checkAllowed(
                "login", "127.0.0.1", 5, Duration.ofSeconds(60)))
                .doesNotThrowAnyException();

        verify(repository).delete(entry);
    }

    @Test
    void reset_deletesOnlySelectedScopeAndClient() {
        rateLimiter.reset("login", "127.0.0.1");

        verify(repository).lockBucket("login", "127.0.0.1");
        verify(repository).deleteByScopeAndClientId("login", "127.0.0.1");
    }

    private RateLimitEntry entry(int attempts, Instant expiresAt, Instant blockedUntil) {
        return RateLimitEntry.builder()
                .scope("login")
                .clientId("127.0.0.1")
                .attemptCount(attempts)
                .windowStartedAt(NOW.minusSeconds(15))
                .windowExpiresAt(expiresAt)
                .blockedUntil(blockedUntil)
                .updatedAt(NOW.minusSeconds(1))
                .build();
    }
}
