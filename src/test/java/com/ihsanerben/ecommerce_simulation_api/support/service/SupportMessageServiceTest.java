package com.ihsanerben.ecommerce_simulation_api.support.service;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.auth.entity.User;
import com.ihsanerben.ecommerce_simulation_api.auth.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SendSupportMessageRequest;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversation;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversationStatus;
import com.ihsanerben.ecommerce_simulation_api.support.exception.SupportConversationNotOpenException;
import com.ihsanerben.ecommerce_simulation_api.support.mapper.SupportMapper;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportConversationRepository;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportMessageRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SupportMessageServiceTest {

    private final SupportConversationRepository conversationRepository =
            mock(SupportConversationRepository.class);
    private final SupportMessageRepository messageRepository = mock(SupportMessageRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SupportMapper mapper = mock(SupportMapper.class);
    private final SupportMessageService service = new SupportMessageService(
            conversationRepository, messageRepository, userRepository, mapper);

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
