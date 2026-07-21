package com.ihsanerben.ecommerce_simulation_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "password_history")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PasswordHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
