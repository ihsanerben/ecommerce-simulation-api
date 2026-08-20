package com.ihsanerben.ecommerce_simulation_api.controller;

import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicConfigControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getConfig_withoutAuthentication_returnsUiConfig() throws Exception {
        mockMvc.perform(get("/api/public/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoUrl")
                        .value("https://www.yerkoygazetesi.com.tr/wp-content/uploads/2024/11/n11.webp"))
                .andExpect(jsonPath("$.primaryColor").value("#f24391"))
                .andExpect(jsonPath("$.supportUrl").value("https://www.n11.com/destek-merkezi"));
    }
}
