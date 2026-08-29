package com.ihsanerben.ecommerce_simulation_api.support.messaging;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.auth.entity.User;
import com.ihsanerben.ecommerce_simulation_api.auth.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationEvent;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SupportEventPublisherTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final SupportEventPublisher publisher =
            new SupportEventPublisher(userRepository, messagingTemplate);

    @Test
    void conversationCreated_notifiesAdmins() {
        given(userRepository.findAllByRole(Role.ADMIN)).willReturn(List.of(
                User.builder().username("admin").build()));
        SupportConversationResponse conversation = conversation(null, null, SupportConversationStatus.WAITING);

        publisher.conversationCreated(conversation);

        verify(messagingTemplate).convertAndSendToUser(
                eq("admin"),
                eq("/queue/support/conversations"),
                argThat(message -> ((SupportConversationEvent) message).type()
                        == SupportConversationEvent.Type.CREATED));
    }

    @Test
    void conversationAssigned_notifiesClient() {
        SupportConversationResponse conversation = conversation(2L, "admin", SupportConversationStatus.OPEN);

        publisher.conversationAssigned(conversation);

        verify(messagingTemplate).convertAndSendToUser(
                eq("client"),
                eq("/queue/support/conversations"),
                argThat(message -> ((SupportConversationEvent) message).type()
                        == SupportConversationEvent.Type.ASSIGNED));
    }

    @Test
    void conversationClosed_notifiesClient() {
        SupportConversationResponse conversation = conversation(2L, "admin", SupportConversationStatus.CLOSED);

        publisher.conversationClosed(conversation);

        verify(messagingTemplate).convertAndSendToUser(
                eq("client"),
                eq("/queue/support/conversations"),
                argThat(message -> ((SupportConversationEvent) message).type()
                        == SupportConversationEvent.Type.CLOSED));
    }

    private SupportConversationResponse conversation(
            Long agentId, String agentUsername, SupportConversationStatus status) {
        return new SupportConversationResponse(
                10L, 1L, "client", agentId, agentUsername, "Teslimat", status, Instant.now());
    }
}
