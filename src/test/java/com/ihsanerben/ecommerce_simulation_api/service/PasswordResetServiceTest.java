package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.entity.*;
import com.ihsanerben.ecommerce_simulation_api.exception.*;
import com.ihsanerben.ecommerce_simulation_api.repository.*;
import com.ihsanerben.ecommerce_simulation_api.security.TokenHashService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock UserRepository users;
    @Mock PasswordResetTokenRepository tokens;
    @Mock PasswordHistoryRepository history;
    @Mock AuthSessionRepository sessions;
    @Mock PasswordEncoder encoder;
    @Mock TokenHashService hashService;
    @Mock EmailService email;
    PasswordResetService service;

    @BeforeEach void setUp() {
        service = new PasswordResetService(users, tokens, history, sessions, encoder, hashService, email);
        ReflectionTestUtils.setField(service, "resetPasswordUrl", "https://frontend/reset-password");
        ReflectionTestUtils.setField(service, "requestCooldownSeconds", 60L);
    }

    @Test void requestReset_forUnknownEmailDoesNotRevealAccount() {
        service.requestReset("unknown@example.com");
        verify(email, never()).sendPasswordReset(anyString(), anyString());
    }

    @Test void requestReset_storesOnlyHashAndEmailsRawToken() {
        User user = user();
        given(users.findByEmailIgnoreCase(user.getEmail())).willReturn(Optional.of(user));
        given(hashService.hash(anyString())).willReturn("token-hash");
        service.requestReset(user.getEmail());
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokens).save(captor.capture());
        verify(email).sendPasswordReset(eq(user.getEmail()), contains("?token="));
        assertThat(captor.getValue().getTokenHash()).isEqualTo("token-hash");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getExpiresAt())
                .isEqualTo(captor.getValue().getCreatedAt().plusMinutes(15));
        verify(tokens).deleteByUserId(user.getId());
    }

    @Test void requestReset_whenRequestedWithinCooldown_keepsExistingTokenAndDoesNotSendAnotherEmail() {
        User user = user();
        given(users.findByEmailIgnoreCase(user.getEmail())).willReturn(Optional.of(user));
        given(tokens.existsByUserIdAndCreatedAtAfter(eq(user.getId()), any(LocalDateTime.class))).willReturn(true);

        service.requestReset(user.getEmail());

        verify(tokens, never()).deleteByUserId(anyLong());
        verify(tokens, never()).save(any());
        verifyNoInteractions(email);
    }

    @Test void resetPassword_rejectsOneOfLastThreePasswords() {
        User user = user();
        PasswordResetToken token = validToken(user);
        given(hashService.hash("raw")).willReturn("hash");
        given(tokens.findByTokenHash("hash")).willReturn(Optional.of(token));
        given(history.findTop3ByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(
                PasswordHistory.builder().passwordHash("old-hash").build()));
        given(encoder.matches("password123", "old-hash")).willReturn(true);
        assertThatThrownBy(() -> service.resetPassword("raw", "password123"))
                .isInstanceOf(PasswordReuseException.class);
        verify(encoder, never()).encode(anyString());
    }

    @Test void resetPassword_rejectsUnknownToken() {
        given(hashService.hash("invalid-token")).willReturn("invalid-hash");
        given(tokens.findByTokenHash("invalid-hash")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("invalid-token", "new-password"))
                .isInstanceOf(InvalidTokenException.class);

        verifyNoInteractions(history, sessions, encoder);
    }

    @Test void resetPassword_updatesPasswordInvalidatesSessionsAndConsumesToken() {
        User user = user();
        PasswordResetToken token = validToken(user);
        AuthSession session = AuthSession.builder().user(user).build();
        given(hashService.hash("raw")).willReturn("hash");
        given(tokens.findByTokenHash("hash")).willReturn(Optional.of(token));
        given(history.findTop3ByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());
        given(history.findAllByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());
        given(encoder.encode("new-password")).willReturn("new-hash");
        given(sessions.findAllByUserIdAndRevokedAtIsNull(1L)).willReturn(List.of(session));
        service.resetPassword("raw", "new-password");
        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(user.getTokenVersion()).isEqualTo(1);
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(session.getRevokedAt()).isNotNull();
        verify(history).save(any(PasswordHistory.class));
    }

    @Test void resetPassword_rejectsExpiredTokenWithoutChangingPasswordOrSessions() {
        User user = user();
        PasswordResetToken token = PasswordResetToken.builder().user(user).tokenHash("hash")
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .expiresAt(LocalDateTime.now().minusMinutes(5)).build();
        given(hashService.hash("raw")).willReturn("hash");
        given(tokens.findByTokenHash("hash")).willReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("raw", "new-password"))
                .isInstanceOf(InvalidTokenException.class);

        assertThat(user.getPassword()).isEqualTo("old-hash");
        verifyNoInteractions(sessions);
    }

    @Test void resetPassword_rejectsAlreadyUsedToken() {
        User user = user();
        PasswordResetToken token = validToken(user);
        token.setUsedAt(LocalDateTime.now().minusMinutes(1));
        given(hashService.hash("raw")).willReturn("hash");
        given(tokens.findByTokenHash("hash")).willReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword("raw", "new-password"))
                .isInstanceOf(InvalidTokenException.class);

        verify(encoder, never()).encode(anyString());
        verifyNoInteractions(sessions);
    }

    private User user() { return User.builder().id(1L).username("ihsan").email("ihsan@example.com")
            .password("old-hash").role(Role.USER).tokenVersion(0).createdAt(LocalDateTime.now()).build(); }
    private PasswordResetToken validToken(User user) { return PasswordResetToken.builder().user(user)
            .tokenHash("hash").createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusMinutes(10)).build(); }
}
