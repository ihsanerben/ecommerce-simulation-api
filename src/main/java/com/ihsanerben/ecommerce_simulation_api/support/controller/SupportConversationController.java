package com.ihsanerben.ecommerce_simulation_api.support.controller;

import com.ihsanerben.ecommerce_simulation_api.security.UserPrincipal;
import com.ihsanerben.ecommerce_simulation_api.support.dto.CreateSupportConversationRequest;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportConversationResponse;
import com.ihsanerben.ecommerce_simulation_api.support.dto.SupportMessageResponse;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public SupportConversationResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSupportConversationRequest request) {
        return service.create(principal.getId(), request);
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
        return service.assign(conversationId, principal.getId());
    }

    @PutMapping("/{conversationId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public SupportConversationResponse close(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId) {
        return service.close(conversationId, principal.getId());
    }

    @GetMapping("/{conversationId}/messages")
    public PagedModel<SupportMessageResponse> messages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long conversationId,
            @PageableDefault(size = 30, sort = "sentAt") Pageable pageable) {
        return new PagedModel<>(service.messages(
                principal.getId(), principal.getRole(), conversationId, pageable));
    }
}
