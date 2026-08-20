package com.ihsanerben.ecommerce_simulation_api.settings.service;

import com.ihsanerben.ecommerce_simulation_api.settings.dto.PublicUiConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys.UI_LOGO_URL;
import static com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys.UI_PRIMARY_COLOR;
import static com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys.UI_SUPPORT_URL;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicUiConfigService {

    private final ApplicationConfigService applicationConfigService;

    public PublicUiConfigResponse getConfig() {
        return new PublicUiConfigResponse(
                applicationConfigService.getValue(UI_LOGO_URL),
                applicationConfigService.getValue(UI_PRIMARY_COLOR),
                applicationConfigService.getValue(UI_SUPPORT_URL));
    }
}
