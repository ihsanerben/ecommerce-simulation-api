package com.ihsanerben.ecommerce_simulation_api.auth.ratelimit.service;

import com.ihsanerben.ecommerce_simulation_api.settings.service.ApplicationConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SensitiveEndpointRateLimitService {

    private static final Policy REGISTER = new Policy(
            "auth.register.requests",
            "auth.register.max-attempts",
            "auth.register.window-seconds");
    private static final Policy FORGOT_PASSWORD = new Policy(
            "auth.forgot-password.requests",
            "auth.forgot-password.max-attempts",
            "auth.forgot-password.window-seconds");

    private final ApplicationConfigService applicationConfigService;
    private final DatabaseRateLimiter rateLimiter;

    public void consumeRegisterRequest(String clientId) {
        consume(REGISTER, clientId);
    }

    public void consumeForgotPasswordRequest(String clientId) {
        consume(FORGOT_PASSWORD, clientId);
    }

    public void resetRegister(String clientId) {
        rateLimiter.reset(REGISTER.scope(), clientId);
    }

    public void resetForgotPassword(String clientId) {
        rateLimiter.reset(FORGOT_PASSWORD.scope(), clientId);
    }

    private void consume(Policy policy, String clientId) {
        int maxAttempts = applicationConfigService.getInteger(policy.maxAttemptsKey());
        int windowSeconds = applicationConfigService.getInteger(policy.windowSecondsKey());
        Duration window = Duration.ofSeconds(windowSeconds);
        rateLimiter.checkAllowed(policy.scope(), clientId, maxAttempts, window);
        rateLimiter.recordAttempt(policy.scope(), clientId, maxAttempts, window);
    }

    private record Policy(String scope, String maxAttemptsKey, String windowSecondsKey) {
    }
}
