package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.LoginRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.AuthResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.DuplicateResourceException;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.security.JwtService;
import com.ihsanerben.ecommerce_simulation_api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user could not be reloaded: '%s'".formatted(request.username())));

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
