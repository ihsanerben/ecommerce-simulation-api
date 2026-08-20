package com.ihsanerben.ecommerce_simulation_api.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chatbot_interactions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @Column(nullable = false, updatable = false, length = 500)
    private String question;

    @Column(nullable = false, updatable = false, length = 50)
    private String category;

    @Column(nullable = false, updatable = false)
    private boolean matched;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;
}
