package com.ihsanerben.ecommerce_simulation_api.settings;

import com.ihsanerben.ecommerce_simulation_api.settings.service.ApplicationConfigService;
import com.ihsanerben.ecommerce_simulation_api.settings.service.PublicUiConfigService;
import com.ihsanerben.ecommerce_simulation_api.settings.dto.PublicUiConfigResponse;
import org.junit.jupiter.api.Test;

import static com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys.UI_LOGO_URL;
import static com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys.UI_PRIMARY_COLOR;
import static com.ihsanerben.ecommerce_simulation_api.settings.ApplicationConfigKeys.UI_SUPPORT_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PublicUiConfigServiceTest {

    private final ApplicationConfigService applicationConfigService = mock(ApplicationConfigService.class);
    private final PublicUiConfigService publicUiConfigService = new PublicUiConfigService(applicationConfigService);

    @Test
    void getConfig_returnsPublicUiValues() {
        given(applicationConfigService.getValue(UI_LOGO_URL)).willReturn("https://cdn.example.com/logo.png");
        given(applicationConfigService.getValue(UI_PRIMARY_COLOR)).willReturn("#f24391");
        given(applicationConfigService.getValue(UI_SUPPORT_URL)).willReturn("https://example.com/support");

        PublicUiConfigResponse result = publicUiConfigService.getConfig();

        assertThat(result.logoUrl()).isEqualTo("https://cdn.example.com/logo.png");
        assertThat(result.primaryColor()).isEqualTo("#f24391");
        assertThat(result.supportUrl()).isEqualTo("https://example.com/support");
    }
}
