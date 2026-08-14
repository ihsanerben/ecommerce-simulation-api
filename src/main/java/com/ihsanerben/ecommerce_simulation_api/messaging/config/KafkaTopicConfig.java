package com.ihsanerben.ecommerce_simulation_api.messaging.config;

import com.ihsanerben.ecommerce_simulation_api.messaging.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic orderCreatedTopic() {
        return singlePartitionTopic(KafkaTopics.ORDER_CREATED);
    }

    @Bean
    NewTopic orderApprovedTopic() {
        return singlePartitionTopic(KafkaTopics.ORDER_APPROVED);
    }

    @Bean
    NewTopic orderCancelledTopic() {
        return singlePartitionTopic(KafkaTopics.ORDER_CANCELLED);
    }

    @Bean
    NewTopic chatbotInteractionTopic() {
        return singlePartitionTopic(KafkaTopics.CHATBOT_INTERACTION);
    }

    private NewTopic singlePartitionTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
