package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.entity.*;
import com.ihsanerben.ecommerce_simulation_api.exception.InvalidTokenException;
import com.ihsanerben.ecommerce_simulation_api.repository.AuthSessionRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import com.ihsanerben.ecommerce_simulation_api.security.*;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final AuthSessionRepository sessionRepository;
    private final RevokedAccessTokenRepository revokedTokenRepository;
    private final JwtService jwtService;
    private final TokenHashService hashService;

    @Transactional
    public AuthTokens create(User user, String userAgent, String ipAddress) {
        return create(user, UUID.randomUUID().toString(), userAgent, ipAddress);
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public AuthTokens rotate(String refreshToken, String userAgent, String ipAddress) {
        try {
            if (!jwtService.isRefreshToken(refreshToken)) throw new InvalidTokenException("Invalid refresh token.");
            String hash = hashService.hash(refreshToken);
            AuthSession old = sessionRepository.findByRefreshTokenHash(hash)
                    .orElseThrow(() -> new InvalidTokenException("Invalid refresh token."));
            LocalDateTime now = LocalDateTime.now();
            if (!old.isActive(now)) {
                revokeFamily(old.getFamilyId(), now);
                throw new InvalidTokenException("Refresh token is expired, revoked, or has already been used.");
            }
            if (!old.getFamilyId().equals(jwtService.extractFamilyId(refreshToken))) {
                revokeFamily(old.getFamilyId(), now);
                throw new InvalidTokenException("Invalid refresh token family.");
            }
            old.setRevokedAt(now);
            old.setLastUsedAt(now);
            return create(old.getUser(), old.getFamilyId(), userAgent, ipAddress);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid or expired refresh token.");
        }
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        LocalDateTime now = LocalDateTime.now();
        if (refreshToken != null) {
            sessionRepository.findByRefreshTokenHash(hashService.hash(refreshToken))
                    .filter(session -> session.getRevokedAt() == null)
                    .ifPresent(session -> session.setRevokedAt(now));
        }
        revokeAccess(accessToken);
    }

    @Transactional
    public void logoutAll(User user, String accessToken) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findAllByUserIdAndRevokedAtIsNull(user.getId())
                .forEach(session -> session.setRevokedAt(now));
        user.setTokenVersion(user.getTokenVersion() + 1);
        revokeAccess(accessToken);
    }

    public boolean isAccessTokenRevoked(String tokenId) {
        return revokedTokenRepository.existsById(tokenId);
    }

    private AuthTokens create(User user, String familyId, String userAgent, String ipAddress) {
        UserPrincipal principal = new UserPrincipal(user);
        String access = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal, familyId);
        sessionRepository.save(AuthSession.builder().user(user).refreshTokenHash(hashService.hash(refresh))
                .familyId(familyId).expiresAt(jwtService.extractExpiration(refresh)).createdAt(LocalDateTime.now())
                .userAgent(truncate(userAgent)).ipAddress(truncate(ipAddress)).build());
        return new AuthTokens(access, refresh);
    }

    private void revokeFamily(String familyId, LocalDateTime now) {
        sessionRepository.findAllByFamilyIdAndRevokedAtIsNull(familyId)
                .forEach(session -> session.setRevokedAt(now));
    }

    private void revokeAccess(String accessToken) {
        if (accessToken == null) return;
        try {
            if (JwtService.ACCESS.equals(jwtService.extractType(accessToken))) {
                revokedTokenRepository.save(RevokedAccessToken.builder()
                        .tokenId(jwtService.extractTokenId(accessToken))
                        .expiresAt(jwtService.extractExpiration(accessToken)).build());
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Logout remains idempotent even if the access token is already invalid.
        }
    }

    private String truncate(String value) {
        return value == null ? null : value.substring(0, Math.min(value.length(), 255));
    }
}
