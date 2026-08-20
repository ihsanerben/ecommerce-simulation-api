package com.ihsanerben.ecommerce_simulation_api.chatbot.repository;

import com.ihsanerben.ecommerce_simulation_api.chatbot.entity.ChatbotInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatbotInteractionRepository extends JpaRepository<ChatbotInteraction, Long> {

    boolean existsByEventId(UUID eventId);
}
