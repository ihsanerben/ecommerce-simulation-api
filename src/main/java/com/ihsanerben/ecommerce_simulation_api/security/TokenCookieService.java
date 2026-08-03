package com.ihsanerben.ecommerce_simulation_api.security;

import com.ihsanerben.ecommerce_simulation_api.service.ApplicationConfigService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.AUTH_COOKIE_DOMAIN;
import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.AUTH_COOKIE_SAME_SITE;
import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.AUTH_COOKIE_SECURE;
import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.JWT_ACCESS_EXPIRATION_MS;
import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.JWT_REFRESH_EXPIRATION_MS;

@Component
@RequiredArgsConstructor
public class TokenCookieService {
    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";
    private final ApplicationConfigService applicationConfigService;

    public void writeTokens(HttpServletResponse response, String access, String refresh) {
        add(response, ACCESS_COOKIE, access, "/", applicationConfigService.getLong(JWT_ACCESS_EXPIRATION_MS));
        add(response, REFRESH_COOKIE, refresh, "/api/auth",
                applicationConfigService.getLong(JWT_REFRESH_EXPIRATION_MS));
    }

    public void clear(HttpServletResponse response) {
        add(response, ACCESS_COOKIE, "", "/", 0);
        add(response, REFRESH_COOKIE, "", "/api/auth", 0);
    }

    public Optional<String> read(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies()).filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue).findFirst();
    }

    private void add(HttpServletResponse response, String name, String value, String path, long maxAgeMs) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(applicationConfigService.getBoolean(AUTH_COOKIE_SECURE))
                .sameSite(applicationConfigService.getValue(AUTH_COOKIE_SAME_SITE))
                .path(path).maxAge(Duration.ofMillis(maxAgeMs));
        String domain = applicationConfigService.getValue(AUTH_COOKIE_DOMAIN);
        if (!domain.isBlank()) {
            builder.domain(domain);
        }
        response.addHeader("Set-Cookie", builder.build().toString());
    }
}
