package com.ihsanerben.ecommerce_simulation_api.support.service;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.auth.entity.User;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.auth.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.support.dto.CreateSupportConversationRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SendSupportMessageRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageDelivery;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageResponse;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversation;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversationStatus;
import com.ihsanerben.ecommerce_simulation_api.support.exception.ConversationAlreadyAssignedException;
import com.ihsanerben.ecommerce_simulation_api.support.exception.SupportConversationNotOpenException;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportMessage;
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
public class SupportConversationService {
    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SupportMapper mapper;

    @Transactional
    public SupportConversationResponse create(Long clientId, CreateSupportConversationRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        SupportConversation conversation = SupportConversation.builder()
                .client(client)
                .subject(request.subject().trim())
                .status(SupportConversationStatus.WAITING)
                .createdAt(Instant.now())
                .build();
        return mapper.toResponse(conversationRepository.save(conversation));
    }

    @Transactional(readOnly = true)
    public Page<SupportConversationResponse> list(Long userId, Role role, Pageable pageable) {
        Page<SupportConversation> conversations = role == Role.ADMIN
                ? conversationRepository.findAll(pageable)
                : conversationRepository.findAllByClientId(userId, pageable);
        return conversations.map(mapper::toResponse);
    }

    @Transactional
    public SupportConversationResponse assign(Long conversationId, Long adminId) {
        SupportConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));
        if (conversation.getAgent() != null && !conversation.getAgent().getId().equals(adminId)) {
            throw new ConversationAlreadyAssignedException();
        }
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found."));
        conversation.setAgent(admin);
        conversation.setStatus(SupportConversationStatus.OPEN);
        return mapper.toResponse(conversation);
    }

    @Transactional
    public SupportConversationResponse close(Long conversationId, Long adminId) {
        SupportConversation conversation = conversationRepository.findByIdAndAgentId(conversationId, adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found."));
        conversation.setStatus(SupportConversationStatus.CLOSED);
        return mapper.toResponse(conversation);
    }

    @Transactional(readOnly = true)
    public Page<SupportMessageResponse> messages(Long userId, Role role, Long conversationId, Pageable pageable) {
        SupportConversation conversation = accessibleConversation(userId, role, conversationId);
        return messageRepository.findAllByConversationId(conversation.getId(), pageable).map(mapper::toResponse);
    }

    @Transactional
    public SupportMessageDelivery send(String username, SendSupportMessageRequest request) {
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        SupportConversation conversation = accessibleConversation(sender.getId(), sender.getRole(), request.conversationId());
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
        String agentUsername = conversation.getAgent() == null ? null : conversation.getAgent().getUsername();
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
