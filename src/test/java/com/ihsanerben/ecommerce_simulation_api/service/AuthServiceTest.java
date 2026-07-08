package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.LoginRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.AuthResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.DuplicateResourceException;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void register_whenUsernameAlreadyTaken_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        given(userRepository.existsByUsername("ihsan")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenEmailAlreadyTaken_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        given(userRepository.existsByUsername("ihsan")).willReturn(false);
        given(userRepository.existsByEmail("ihsan@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenRequestIsValid_savesUserAndReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        given(userRepository.existsByUsername("ihsan")).willReturn(false);
        given(userRepository.existsByEmail("ihsan@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("hashed-password");
        given(jwtService.generateToken(any())).willReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("ihsan");
        assertThat(savedUser.getEmail()).isEqualTo("ihsan@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.username()).isEqualTo("ihsan");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void login_whenCredentialsAreValid_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("ihsan", "password123");
        User user = User.builder()
                .id(1L)
                .username("ihsan")
                .email("ihsan@example.com")
                .password("hashed-password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findByUsername("ihsan")).willReturn(Optional.of(user));
        given(jwtService.generateToken(any())).willReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.username()).isEqualTo("ihsan");
    }

    @Test
    void login_whenCredentialsAreInvalid_propagatesBadCredentialsException() {
        LoginRequest request = new LoginRequest("ihsan", "wrong-password");
        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByUsername(anyString());
    }
}
