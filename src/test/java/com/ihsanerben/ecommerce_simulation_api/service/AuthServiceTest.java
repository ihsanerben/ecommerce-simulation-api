package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.*;
import com.ihsanerben.ecommerce_simulation_api.entity.*;
import com.ihsanerben.ecommerce_simulation_api.exception.DuplicateResourceException;
import com.ihsanerben.ecommerce_simulation_api.exception.PasswordReuseException;
import com.ihsanerben.ecommerce_simulation_api.repository.*;
import com.ihsanerben.ecommerce_simulation_api.security.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock SessionService sessionService;
    @Mock PasswordHistoryRepository historyRepository;
    @Mock JwtService jwtService;
    AuthService service;

    @BeforeEach void setUp() {
        service = new AuthService(userRepository, passwordEncoder, authenticationManager,
                sessionService, historyRepository, jwtService);
    }

    @Test void register_whenUsernameExists_throwsConflict() {
        given(userRepository.existsByUsername("ihsan")).willReturn(true);
        assertThatThrownBy(() -> service.register(request(), null, null))
                .isInstanceOf(DuplicateResourceException.class).hasMessageContaining("username");
        verify(userRepository, never()).save(any());
    }

    @Test void register_whenEmailExists_throwsConflict() {
        given(userRepository.existsByEmail("ihsan@example.com")).willReturn(true);
        assertThatThrownBy(() -> service.register(request(), null, null))
                .isInstanceOf(DuplicateResourceException.class).hasMessageContaining("email");
    }

    @Test void register_savesPasswordHistoryAndReturnsTokensOutsideResponseBody() {
        given(passwordEncoder.encode("password123")).willReturn("hash");
        given(sessionService.create(any(), any(), any())).willReturn(new AuthTokens("access", "refresh"));
        AuthResult result = service.register(request(), "agent", "127.0.0.1");
        verify(userRepository).save(any(User.class));
        verify(historyRepository).save(any(PasswordHistory.class));
        assertThat(result.response().username()).isEqualTo("ihsan");
        assertThat(result.tokens().accessToken()).isEqualTo("access");
    }

    @Test void login_withValidCredentials_createsServerSideSession() {
        User user = user();
        given(userRepository.findByUsername("ihsan")).willReturn(Optional.of(user));
        given(sessionService.create(user, "agent", "ip")).willReturn(new AuthTokens("access", "refresh"));
        AuthResult result = service.login(new LoginRequest("ihsan", "password123"), "agent", "ip");
        verify(authenticationManager).authenticate(any());
        assertThat(result.response().role()).isEqualTo(Role.USER);
    }

    @Test void login_withInvalidCredentials_propagatesUnauthorized() {
        doThrow(new BadCredentialsException("bad")).when(authenticationManager).authenticate(any());
        assertThatThrownBy(() -> service.login(new LoginRequest("ihsan", "bad"), null, null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test void changePassword_withWrongCurrentPassword_doesNotChangePasswordOrSessions() {
        User user = user();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "hash")).willReturn(false);

        assertThatThrownBy(() -> service.changePassword(1L,
                new ChangePasswordRequest("wrong-password", "new-password"), "access"))
                .isInstanceOf(BadCredentialsException.class);

        verify(passwordEncoder, never()).encode(anyString());
        verifyNoInteractions(sessionService);
    }

    @Test void changePassword_withRecentPassword_rejectsReuse() {
        User user = user();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "hash")).willReturn(true);
        given(historyRepository.findTop3ByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(
                PasswordHistory.builder().passwordHash("recent-hash").build()));
        given(passwordEncoder.matches("old-password", "recent-hash")).willReturn(true);

        assertThatThrownBy(() -> service.changePassword(1L,
                new ChangePasswordRequest("password123", "old-password"), "access"))
                .isInstanceOf(PasswordReuseException.class);

        verifyNoInteractions(sessionService);
    }

    @Test void changePassword_withValidRequest_updatesHistoryAndRevokesEverySession() {
        User user = user();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "hash")).willReturn(true);
        given(historyRepository.findTop3ByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());
        given(historyRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());
        given(passwordEncoder.encode("new-password")).willReturn("new-hash");

        service.changePassword(1L,
                new ChangePasswordRequest("password123", "new-password"), "access");

        assertThat(user.getPassword()).isEqualTo("new-hash");
        verify(historyRepository).save(any(PasswordHistory.class));
        verify(sessionService).logoutAll(user, "access");
    }

    private RegisterRequest request() { return new RegisterRequest("ihsan", "ihsan@example.com", "password123"); }
    private User user() { return User.builder().id(1L).username("ihsan").email("ihsan@example.com")
            .password("hash").role(Role.USER).tokenVersion(0).createdAt(LocalDateTime.now()).build(); }
}
