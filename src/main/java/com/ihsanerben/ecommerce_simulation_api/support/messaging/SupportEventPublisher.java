package com.ihsanerben.ecommerce_simulation_api.support.messaging;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.auth.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationEvent;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupportEventPublisher {

    private static final String DESTINATION = "/queue/support/conversations";

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void conversationCreated(SupportConversationResponse conversation) {
        SupportConversationEvent event = new SupportConversationEvent(
                SupportConversationEvent.Type.CREATED, conversation);
        userRepository.findAllByRole(Role.ADMIN).forEach(admin ->
                messagingTemplate.convertAndSendToUser(admin.getUsername(), DESTINATION, event));
    }

    public void conversationAssigned(SupportConversationResponse conversation) {
        SupportConversationEvent event = new SupportConversationEvent(
                SupportConversationEvent.Type.ASSIGNED, conversation);
        messagingTemplate.convertAndSendToUser(conversation.clientUsername(), DESTINATION, event);
    }

    public void conversationClosed(SupportConversationResponse conversation) {
        SupportConversationEvent event = new SupportConversationEvent(
                SupportConversationEvent.Type.CLOSED, conversation);
        messagingTemplate.convertAndSendToUser(conversation.clientUsername(), DESTINATION, event);
    }

    public void messageSent(SupportMessageDelivery delivery) {
        messagingTemplate.convertAndSendToUser(
                delivery.clientUsername(), "/queue/support", delivery.message());
        if (delivery.agentUsername() != null && !delivery.agentUsername().equals(delivery.clientUsername())) {
            messagingTemplate.convertAndSendToUser(
                    delivery.agentUsername(), "/queue/support", delivery.message());
        }
    }
}
