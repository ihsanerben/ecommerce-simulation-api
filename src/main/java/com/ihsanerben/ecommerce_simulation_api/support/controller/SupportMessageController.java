package com.ihsanerben.ecommerce_simulation_api.support.controller;

import com.ihsanerben.ecommerce_simulation_api.auth.security.UserPrincipal;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SendSupportMessageRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageDelivery;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageResponse;
import com.ihsanerben.ecommerce_simulation_api.support.messaging.SupportEventPublisher;
import com.ihsanerben.ecommerce_simulation_api.support.service.SupportMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class SupportMessageController {
    private final SupportMessageService service;
    private final SupportEventPublisher eventPublisher;

    @GetMapping("/api/support/conversations/{conversationId}/messages")
    @ResponseBody
    public PagedModel<SupportMessageResponse> messages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @PageableDefault(size = 30, sort = "sentAt") Pageable pageable) {
        return new PagedModel<>(service.messages(
                principal.getId(), principal.getRole(), conversationId, pageable));
    }

    @MessageMapping("/support.send")
    public void send(@Valid SendSupportMessageRequest request, Principal principal) {
        SupportMessageDelivery delivery = service.send(principal.getName(), request);
        eventPublisher.messageSent(delivery);
    }
}
