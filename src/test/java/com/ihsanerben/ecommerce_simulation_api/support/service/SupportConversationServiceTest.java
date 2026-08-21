package com.ihsanerben.ecommerce_simulation_api.support.service;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.User;
import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.auth.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SendSupportMessageRequest;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversation;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversationStatus;
import com.ihsanerben.ecommerce_simulation_api.support.mapper.SupportMapper;
import com.ihsanerben.ecommerce_simulation_api.support.exception.SupportConversationNotOpenException;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportConversationRepository;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportMessageRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

class SupportConversationServiceTest {

    private final SupportConversationRepository conversationRepository =
            mock(SupportConversationRepository.class);
    private final SupportMessageRepository messageRepository = mock(SupportMessageRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SupportMapper mapper = mock(SupportMapper.class);
    private final SupportConversationService service = new SupportConversationService(
            conversationRepository,
            messageRepository,
            userRepository,
            mapper);

    @Test
    void close_whenAssignedToAdmin_closesConversation() {
        User admin = User.builder().id(2L).username("admin").build();
        SupportConversation conversation = SupportConversation.builder()
                .id(10L)
                .agent(admin)
                .status(SupportConversationStatus.OPEN)
                .build();
        SupportConversationResponse response = new SupportConversationResponse(
                10L, 1L, "client", 2L, "admin", "Teslimat", SupportConversationStatus.CLOSED, Instant.now());
        given(conversationRepository.findByIdAndAgentId(10L, 2L)).willReturn(Optional.of(conversation));
        given(mapper.toResponse(conversation)).willReturn(response);

        SupportConversationResponse result = service.close(10L, 2L);

        assertThat(conversation.getStatus()).isEqualTo(SupportConversationStatus.CLOSED);
        assertThat(result.status()).isEqualTo(SupportConversationStatus.CLOSED);
    }

    @Test
    void close_whenNotAssignedToAdmin_throwsNotFound() {
        given(conversationRepository.findByIdAndAgentId(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.close(10L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void send_whenConversationHasNoAgent_rejectsMessageWithoutSaving() {
        User client = User.builder().id(1L).username("client").role(Role.USER).build();
        SupportConversation conversation = SupportConversation.builder()
                .id(10L)
                .client(client)
                .status(SupportConversationStatus.WAITING)
                .build();
        given(userRepository.findByUsername("client")).willReturn(Optional.of(client));
        given(conversationRepository.findById(10L)).willReturn(Optional.of(conversation));

        assertThatThrownBy(() -> service.send(
                "client", new SendSupportMessageRequest(10L, "Merhaba")))
                .isInstanceOf(SupportConversationNotOpenException.class);
        verify(messageRepository, never()).save(any());
    }
}
