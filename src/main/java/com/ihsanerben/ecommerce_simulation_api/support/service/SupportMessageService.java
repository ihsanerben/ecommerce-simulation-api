package com.ihsanerben.ecommerce_simulation_api.support.service;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.auth.entity.User;
import com.ihsanerben.ecommerce_simulation_api.auth.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SendSupportMessageRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageDelivery;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageResponse;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversation;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversationStatus;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportMessage;
import com.ihsanerben.ecommerce_simulation_api.support.exception.SupportConversationNotOpenException;
import com.ihsanerben.ecommerce_simulation_api.support.mapper.SupportMapper;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportConversationRepository;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SupportMapper mapper;

    @Transactional(readOnly = true)
    public Page<SupportMessageResponse> messages(Long userId, Role role, Long conversationId, Pageable pageable) {
        SupportConversation conversation = accessibleConversation(userId, role, conversationId);
        return messageRepository.findAllByConversationId(conversation.getId(), pageable).map(mapper::toResponse);
    }

    @Transactional
    public SupportMessageDelivery send(String username, SendSupportMessageRequest request) {
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        SupportConversation conversation = accessibleConversation(
                sender.getId(), sender.getRole(), request.conversationId());
        if (conversation.getAgent() == null || conversation.getStatus() != SupportConversationStatus.OPEN) {
            throw new SupportConversationNotOpenException();
        }
        SupportMessage message = SupportMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content().trim())
                .sentAt(Instant.now())
                .build();
        SupportMessageResponse response = mapper.toResponse(messageRepository.save(message));
        String agentUsername = conversation.getAgent().getUsername();
        return new SupportMessageDelivery(response, conversation.getClient().getUsername(), agentUsername);
    }

    private SupportConversation accessibleConversation(Long userId, Role role, Long conversationId) {
        SupportConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));
        boolean client = conversation.getClient().getId().equals(userId);
        boolean assignedAgent = conversation.getAgent() != null
                && conversation.getAgent().getId().equals(userId);
        boolean waitingAdmin = role == Role.ADMIN && conversation.getAgent() == null;
        if (!client && !assignedAgent && !waitingAdmin) {
            throw new ResourceNotFoundException("Conversation not found.");
        }
        return conversation;
    }
}
