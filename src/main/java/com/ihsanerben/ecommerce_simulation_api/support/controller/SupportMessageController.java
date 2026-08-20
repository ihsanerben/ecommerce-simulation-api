package com.ihsanerben.ecommerce_simulation_api.support.controller;

import com.ihsanerben.ecommerce_simulation_api.support.dto.SendSupportMessageRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageDelivery;
import com.ihsanerben.ecommerce_simulation_api.support.service.SupportConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SupportMessageController {
    private final SupportConversationService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/support.send")
    public void send(@Valid SendSupportMessageRequest request, Principal principal) {
        SupportMessageDelivery delivery = service.send(principal.getName(), request);
        messagingTemplate.convertAndSendToUser(delivery.clientUsername(), "/queue/support", delivery.message());
        if (delivery.agentUsername() != null && !delivery.agentUsername().equals(delivery.clientUsername())) {
            messagingTemplate.convertAndSendToUser(delivery.agentUsername(), "/queue/support", delivery.message());
        }
    }
}
