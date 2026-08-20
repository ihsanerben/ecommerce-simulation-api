package com.ihsanerben.ecommerce_simulation_api.support.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.security.TokenCookieService;
import com.ihsanerben.ecommerce_simulation_api.support.dto.CreateSupportConversationRequest;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportConversationRepository;
import com.ihsanerben.ecommerce_simulation_api.support.repository.SupportMessageRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SupportConversationControllerIT extends AbstractIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SupportMessageRepository messageRepository;
    @Autowired SupportConversationRepository conversationRepository;
    @Autowired UserRepository userRepository;

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userCreatesConversationAndOnlySeesOwnConversation() throws Exception {
        Cookie ownerCookie = register("support-owner");
        Cookie otherCookie = register("support-other");

        mockMvc.perform(post("/api/support/conversations")
                        .cookie(ownerCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateSupportConversationRequest("Teslimat sorunu"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.clientUsername").value("support-owner"));

        mockMvc.perform(get("/api/support/conversations").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/support/conversations").cookie(otherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    private Cookie register(String username) throws Exception {
        RegisterRequest request = new RegisterRequest(username, username + "@example.com", "password123");
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getCookie(TokenCookieService.ACCESS_COOKIE);
    }
}
