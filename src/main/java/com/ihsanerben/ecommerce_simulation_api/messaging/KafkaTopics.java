package com.ihsanerben.ecommerce_simulation_api.messaging;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_APPROVED = "order.approved";
    public static final String ORDER_CANCELLED = "order.cancelled";
    private KafkaTopics() {
    }
}
