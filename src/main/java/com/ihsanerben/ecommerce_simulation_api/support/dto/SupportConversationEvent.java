package com.ihsanerben.ecommerce_simulation_api.support.dto;

public record SupportConversationEvent(
        Type type,
        SupportConversationResponse conversation) {

    public enum Type {
        CREATED,
        ASSIGNED,
        CLOSED
    }
}
