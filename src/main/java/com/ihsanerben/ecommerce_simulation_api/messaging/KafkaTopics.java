package com.ihsanerben.ecommerce_simulation_api.messaging;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order.created";
    public static final String CHATBOT_REQUEST = "chatbot.request";
    public static final String CHATBOT_RESPONSE = "chatbot.response";

    private KafkaTopics() {
    }
}
