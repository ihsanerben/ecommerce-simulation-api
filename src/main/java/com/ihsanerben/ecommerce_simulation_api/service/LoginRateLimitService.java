package com.ihsanerben.ecommerce_simulation_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class LoginRateLimitService {

    private static final String SCOPE = "auth.login.failures";
    private static final String MAX_ATTEMPTS_KEY = "auth.login.max-attempts";
    private static final String WINDOW_SECONDS_KEY = "auth.login.window-seconds";

    private final ApplicationConfigService applicationConfigService;
    private final DatabaseRateLimiter rateLimiter;

    public <T> T execute(String ipAddress, Supplier<T> loginAttempt) {
        RateLimitPolicy policy = policy();
        rateLimiter.checkAllowed(SCOPE, ipAddress, policy.maxAttempts(), policy.window());
        try {
            T result = loginAttempt.get();
            reset(ipAddress);
            return result;
        } catch (BadCredentialsException exception) {
            rateLimiter.recordAttempt(SCOPE, ipAddress, policy.maxAttempts(), policy.window());
            throw exception;
        }
    }

    public void reset(String ipAddress) {
        rateLimiter.reset(SCOPE, ipAddress);
    }

    private RateLimitPolicy policy() {
        int maxAttempts = applicationConfigService.getInteger(MAX_ATTEMPTS_KEY);
        int windowSeconds = applicationConfigService.getInteger(WINDOW_SECONDS_KEY);
        return new RateLimitPolicy(maxAttempts, Duration.ofSeconds(windowSeconds));
    }

    private record RateLimitPolicy(int maxAttempts, Duration window) {
    }
}
