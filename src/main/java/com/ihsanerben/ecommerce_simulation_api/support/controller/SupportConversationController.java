package com.ihsanerben.ecommerce_simulation_api.support.controller;

import com.ihsanerben.ecommerce_simulation_api.auth.security.UserPrincipal;
import com.ihsanerben.ecommerce_simulation_api.support.dto.CreateSupportConversationRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.messaging.SupportEventPublisher;
import com.ihsanerben.ecommerce_simulation_api.support.service.SupportConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support/conversations")
@RequiredArgsConstructor
public class SupportConversationController {
    private final SupportConversationService service;
    private final SupportEventPublisher eventPublisher;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public SupportConversationResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSupportConversationRequest request) {
        SupportConversationResponse conversation = service.create(principal.getId(), request);
        eventPublisher.conversationCreated(conversation);
        return conversation;
    }

    @GetMapping
    public PagedModel<SupportConversationResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return new PagedModel<>(service.list(principal.getId(), principal.getRole(), pageable));
    }

    @PutMapping("/{conversationId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public SupportConversationResponse assign(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId) {
        SupportConversationResponse conversation = service.assign(conversationId, principal.getId());
        eventPublisher.conversationAssigned(conversation);
        return conversation;
    }

    @PutMapping("/{conversationId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public SupportConversationResponse close(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId) {
        SupportConversationResponse conversation = service.close(conversationId, principal.getId());
        eventPublisher.conversationClosed(conversation);
        return conversation;
    }

}
