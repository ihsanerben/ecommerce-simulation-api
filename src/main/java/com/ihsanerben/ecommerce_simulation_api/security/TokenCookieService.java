package com.ihsanerben.ecommerce_simulation_api.security;

import com.ihsanerben.ecommerce_simulation_api.config.AuthCookieProperties;
import com.ihsanerben.ecommerce_simulation_api.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TokenCookieService {
    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";
    private final JwtProperties jwt;
    private final AuthCookieProperties cookieProperties;

    public void writeTokens(HttpServletResponse response, String access, String refresh) {
        add(response, ACCESS_COOKIE, access, "/", jwt.accessExpirationMs());
        add(response, REFRESH_COOKIE, refresh, "/api/auth", jwt.refreshExpirationMs());
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
                .httpOnly(true).secure(cookieProperties.secure()).sameSite(cookieProperties.sameSite())
                .path(path).maxAge(Duration.ofMillis(maxAgeMs));
        if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
            builder.domain(cookieProperties.domain());
        }
        response.addHeader("Set-Cookie", builder.build().toString());
    }
}
