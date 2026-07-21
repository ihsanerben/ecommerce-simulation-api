package com.ihsanerben.ecommerce_simulation_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "auth_sessions", indexes = @Index(name = "idx_auth_session_token_hash", columnList = "refresh_token_hash", unique = true))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @Column(name = "refresh_token_hash", nullable = false, length = 64, unique = true)
    private String refreshTokenHash;
    @Column(nullable = false, length = 36)
    private String familyId;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private String userAgent;
    private String ipAddress;

    public boolean isActive(LocalDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
