package com.ihsanerben.ecommerce_simulation_api.support.mapper;

import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageResponse;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportConversation;
import com.ihsanerben.ecommerce_simulation_api.support.entity.SupportMessage;
import org.springframework.stereotype.Component;

@Component
public class SupportMapper {
    public SupportConversationResponse toResponse(SupportConversation conversation) {
        Long agentId = conversation.getAgent() == null ? null : conversation.getAgent().getId();
        String agentUsername = conversation.getAgent() == null ? null : conversation.getAgent().getUsername();
        return new SupportConversationResponse(
                conversation.getId(), conversation.getClient().getId(), conversation.getClient().getUsername(),
                agentId, agentUsername, conversation.getSubject(), conversation.getStatus(), conversation.getCreatedAt());
    }

    public SupportMessageResponse toResponse(SupportMessage message) {
        return new SupportMessageResponse(
                message.getId(), message.getConversation().getId(), message.getSender().getId(),
                message.getSender().getUsername(), message.getSender().getRole(), message.getContent(), message.getSentAt());
    }
}
