package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.*;
import com.ihsanerben.ecommerce_simulation_api.dto.response.AuthResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.*;
import com.ihsanerben.ecommerce_simulation_api.exception.*;
import com.ihsanerben.ecommerce_simulation_api.repository.*;
import com.ihsanerben.ecommerce_simulation_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResult register(RegisterRequest request, String userAgent, String ipAddress) {
        if (userRepository.existsByUsername(request.username()))
            throw new DuplicateResourceException("User", "username", request.username());
        if (userRepository.existsByEmail(request.email()))
            throw new DuplicateResourceException("User", "email", request.email());

        String hash = passwordEncoder.encode(request.password());
        User user = User.builder().username(request.username()).email(request.email()).password(hash)
                .role(Role.USER).tokenVersion(0).createdAt(LocalDateTime.now()).build();
        userRepository.save(user);
        passwordHistoryRepository.save(PasswordHistory.builder().user(user).passwordHash(hash)
                .createdAt(LocalDateTime.now()).build());
        return result(user, sessionService.create(user, userAgent, ipAddress));
    }

    @Transactional
    public AuthResult login(LoginRequest request, String userAgent, String ipAddress) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Authenticated user could not be reloaded."));
        return result(user, sessionService.create(user, userAgent, ipAddress));
    }

    @Transactional
    public AuthResult refresh(String refreshToken, String userAgent, String ipAddress) {
        AuthTokens tokens = sessionService.rotate(refreshToken, userAgent, ipAddress);
        User user = userRepository.findByUsername(jwtService.extractUsername(tokens.accessToken()))
                .orElseThrow(() -> new InvalidTokenException("Token user no longer exists."));
        return result(user, tokens);
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        sessionService.logout(refreshToken, accessToken);
    }

    @Transactional
    public void logoutAll(Long userId, String accessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        sessionService.logoutAll(user, accessToken);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String accessToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword()))
            throw new BadCredentialsException("Current password is incorrect.");

        List<PasswordHistory> recent = passwordHistoryRepository
                .findTop3ByUserIdOrderByCreatedAtDesc(userId);
        if (recent.stream().anyMatch(old -> passwordEncoder.matches(
                request.newPassword(), old.getPasswordHash())))
            throw new PasswordReuseException("New password must be different from the last 3 passwords.");

        LocalDateTime now = LocalDateTime.now();
        String newHash = passwordEncoder.encode(request.newPassword());
        user.setPassword(newHash);
        passwordHistoryRepository.save(PasswordHistory.builder().user(user).passwordHash(newHash)
                .createdAt(now).build());
        trimPasswordHistory(userId);
        sessionService.logoutAll(user, accessToken);
    }

    private void trimPasswordHistory(Long userId) {
        List<PasswordHistory> all = passwordHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        if (all.size() > 3) passwordHistoryRepository.deleteAll(all.subList(3, all.size()));
    }

    private AuthResult result(User user, AuthTokens tokens) {
        return new AuthResult(new AuthResponse(user.getUsername(), user.getRole()), tokens);
    }
}
