package com.ihsanerben.ecommerce_simulation_api.security;

import com.ihsanerben.ecommerce_simulation_api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";
    private final JwtProperties properties;

    public String generateAccessToken(UserPrincipal principal) {
        return build(principal, ACCESS, properties.accessExpirationMs(), UUID.randomUUID().toString(), null);
    }

    public String generateRefreshToken(UserPrincipal principal, String familyId) {
        return build(principal, REFRESH, properties.refreshExpirationMs(), UUID.randomUUID().toString(), familyId);
    }

    // Kept for API clients and tests that explicitly need a standalone access token.
    public String generateToken(UserPrincipal principal) {
        return generateAccessToken(principal);
    }

    private String build(UserPrincipal principal, String type, long lifetime, String jti, String familyId) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(principal.getUsername())
                .id(jti)
                .claim("userId", principal.getId())
                .claim("role", principal.getRole().name())
                .claim("type", type)
                .claim("tokenVersion", principal.getTokenVersion())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + lifetime));
        if (familyId != null) builder.claim("familyId", familyId);
        return builder.signWith(signingKey()).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) { return parse(token).getSubject(); }
    public String extractTokenId(String token) { return parse(token).getId(); }
    public String extractType(String token) { return parse(token).get("type", String.class); }
    public String extractFamilyId(String token) { return parse(token).get("familyId", String.class); }
    public LocalDateTime extractExpiration(String token) {
        return LocalDateTime.ofInstant(parse(token).getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public boolean isAccessTokenValid(String token, UserPrincipal principal) {
        Claims claims = parse(token);
        Integer version = claims.get("tokenVersion", Integer.class);
        return ACCESS.equals(claims.get("type", String.class))
                && claims.getSubject().equals(principal.getUsername())
                && version != null && version.equals(principal.getTokenVersion());
    }

    public boolean isRefreshToken(String token) { return REFRESH.equals(extractType(token)); }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        return isAccessTokenValid(token, principal);
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
