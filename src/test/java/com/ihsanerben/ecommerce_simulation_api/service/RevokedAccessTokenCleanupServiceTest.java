package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RevokedAccessTokenCleanupServiceTest {
    @Mock RevokedAccessTokenRepository repository;

    @Test
    void deleteExpiredTokens_deletesOnlyEntriesOlderThanCurrentTime() {
        LocalDateTime beforeCleanup = LocalDateTime.now();

        new RevokedAccessTokenCleanupService(repository).deleteExpiredTokens();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).deleteByExpiresAtBefore(cutoff.capture());
        assertThat(cutoff.getValue()).isBetween(beforeCleanup, LocalDateTime.now());
    }
}
