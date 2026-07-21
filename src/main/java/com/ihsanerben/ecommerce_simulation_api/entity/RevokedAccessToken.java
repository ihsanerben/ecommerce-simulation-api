package com.ihsanerben.ecommerce_simulation_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_access_tokens")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RevokedAccessToken {
    @Id @Column(length = 36)
    private String tokenId;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
