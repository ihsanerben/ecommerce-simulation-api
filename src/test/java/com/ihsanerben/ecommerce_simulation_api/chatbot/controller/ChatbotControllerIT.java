package com.ihsanerben.ecommerce_simulation_api.chatbot.controller;

import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatbotControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendMessage_usesKafkaRequestResponseFlow() throws Exception {
        mockMvc.perform(post("/api/chatbot/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Kargom ne zaman gelir?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.message").value(
                        "Siparişlerinizi Siparişlerim ekranından takip edebilirsiniz. "
                                + "Teslimat süresi satıcıya göre değişebilir."));
    }

    @Test
    void sendMessage_whenBlank_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/chatbot/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.message").value("Message is required."));
    }
}
