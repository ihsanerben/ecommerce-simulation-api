package com.ihsanerben.ecommerce_simulation_api.config;

public final class ApplicationConfigKeys {

    public static final String JWT_ACCESS_EXPIRATION_MS = "jwt.access-expiration-ms";
    public static final String JWT_REFRESH_EXPIRATION_MS = "jwt.refresh-expiration-ms";
    public static final String AUTH_COOKIE_SECURE = "auth.cookie.secure";
    public static final String AUTH_COOKIE_SAME_SITE = "auth.cookie.same-site";
    public static final String AUTH_COOKIE_DOMAIN = "auth.cookie.domain";
    public static final String PASSWORD_RESET_COOLDOWN_SECONDS =
            "auth.password-reset-request-cooldown-seconds";
    public static final String FRONTEND_RESET_PASSWORD_URL = "frontend.reset-password-url";
    public static final String SECURITY_ALLOWED_ORIGIN = "security.allowed-origin";

    private ApplicationConfigKeys() {
    }
}
