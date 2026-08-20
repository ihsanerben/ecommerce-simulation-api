package com.ihsanerben.ecommerce_simulation_api.settings.controller;

import com.ihsanerben.ecommerce_simulation_api.settings.dto.PublicUiConfigResponse;
import com.ihsanerben.ecommerce_simulation_api.settings.service.PublicUiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/config")
@RequiredArgsConstructor
@Tag(name = "Public Configuration", description = "Public UI configuration")
public class PublicConfigController {

    private final PublicUiConfigService publicUiConfigService;

    @GetMapping
    @Operation(summary = "Get public UI configuration")
    @ApiResponse(responseCode = "200", description = "UI configuration")
    public PublicUiConfigResponse getConfig() {
        return publicUiConfigService.getConfig();
    }
}
