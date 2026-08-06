package com.ihsanerben.ecommerce_simulation_api.chatbot.controller;

import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotRequest;
import com.ihsanerben.ecommerce_simulation_api.chatbot.dto.ChatbotResponse;
import com.ihsanerben.ecommerce_simulation_api.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/messages")
    public ChatbotResponse sendMessage(@Valid @RequestBody ChatbotRequest request) {
        return chatbotService.ask(request.message());
    }
}
