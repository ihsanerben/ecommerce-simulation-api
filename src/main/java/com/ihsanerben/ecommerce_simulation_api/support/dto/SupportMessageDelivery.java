package com.ihsanerben.ecommerce_simulation_api.support.dto;

public record SupportMessageDelivery(
        SupportMessageResponse message, String clientUsername, String agentUsername) {
}
