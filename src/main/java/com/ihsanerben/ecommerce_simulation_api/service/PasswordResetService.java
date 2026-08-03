package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.entity.*;
import com.ihsanerben.ecommerce_simulation_api.exception.*;
import com.ihsanerben.ecommerce_simulation_api.repository.*;
import com.ihsanerben.ecommerce_simulation_api.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.FRONTEND_RESET_PASSWORD_URL;
import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.PASSWORD_RESET_COOLDOWN_SECONDS;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordHistoryRepository historyRepository;
    private final AuthSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHashService hashService;
    private final EmailService emailService;
    private final ApplicationConfigService applicationConfigService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            LocalDateTime now = LocalDateTime.now();
            if (resetTokenRepository.existsByUserIdAndCreatedAtAfter(
                    user.getId(), now.minusSeconds(
                            applicationConfigService.getLong(PASSWORD_RESET_COOLDOWN_SECONDS)))) return;
            resetTokenRepository.deleteByUserId(user.getId());
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            resetTokenRepository.save(PasswordResetToken.builder().user(user).tokenHash(hashService.hash(rawToken))
                    .createdAt(now).expiresAt(now.plusMinutes(15)).build());
            emailService.sendPasswordReset(user.getEmail(),
                    applicationConfigService.getValue(FRONTEND_RESET_PASSWORD_URL) + "?token=" + rawToken);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = resetTokenRepository.findByTokenHash(hashService.hash(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token."));
        LocalDateTime now = LocalDateTime.now();
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now))
            throw new InvalidTokenException("Invalid or expired password reset token.");

        User user = token.getUser();
        List<PasswordHistory> recent = historyRepository.findTop3ByUserIdOrderByCreatedAtDesc(user.getId());
        if (recent.stream().anyMatch(old -> passwordEncoder.matches(newPassword, old.getPasswordHash())))
            throw new PasswordReuseException("New password must be different from the last 3 passwords.");

        String newHash = passwordEncoder.encode(newPassword);
        user.setPassword(newHash);
        user.setTokenVersion(user.getTokenVersion() + 1);
        historyRepository.save(PasswordHistory.builder().user(user).passwordHash(newHash).createdAt(now).build());
        trimHistory(user.getId());
        sessionRepository.findAllByUserIdAndRevokedAtIsNull(user.getId()).forEach(session -> session.setRevokedAt(now));
        token.setUsedAt(now);
    }

    private void trimHistory(Long userId) {
        List<PasswordHistory> all = historyRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        if (all.size() > 3) historyRepository.deleteAll(all.subList(3, all.size()));
    }
}
