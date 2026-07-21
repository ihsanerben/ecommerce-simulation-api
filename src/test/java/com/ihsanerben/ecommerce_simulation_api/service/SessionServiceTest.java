package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.repository.AuthSessionRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import com.ihsanerben.ecommerce_simulation_api.security.JwtService;
import com.ihsanerben.ecommerce_simulation_api.security.TokenHashService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock AuthSessionRepository sessionRepository;
    @Mock RevokedAccessTokenRepository revokedTokenRepository;
    @Mock JwtService jwtService;
    @Mock TokenHashService hashService;
    SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessionRepository, revokedTokenRepository, jwtService, hashService);
    }

    @Test
    void logout_withExpiredAccessToken_doesNotAddItToBlacklist() {
        when(jwtService.extractType("expired-access-token"))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        service.logout(null, "expired-access-token");

        verify(revokedTokenRepository, never()).save(any());
        verifyNoInteractions(sessionRepository);
    }
}
