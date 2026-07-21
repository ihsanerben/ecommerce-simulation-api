package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.LoginRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.RevokedAccessTokenRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RevokedAccessTokenRepository revokedAccessTokenRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll();
        userRepository.deleteAll();
        revokedAccessTokenRepository.deleteAll();
    }

    @Test
    void register_withValidRequest_returns201AndHttpOnlyCookies() throws Exception {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(jsonPath("$.username").value("ihsan"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_withDuplicateUsername_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest("ihsan", "other@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("ihsan", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void login_withValidCredentials_returns200AndHttpOnlyCookies() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("ihsan", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("ihsan", "ihsan@example.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("ihsan", "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPassword_forUnknownEmail_returnsGenericAcceptedResponse() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "If an account exists for this email, a reset link has been sent."));
    }

    @Test
    void me_withAuthenticatedUser_returnsCurrentUser() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();

        mockMvc.perform(get("/api/auth/me").cookie(registration.getCookie("access_token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ihsan"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void changePassword_withValidCurrentPassword_revokesAllSessionsAndRequiresLoginAgain() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Cookie firstRefresh = registration.getCookie("refresh_token");

        var secondLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("ihsan", "password123"))))
                .andExpect(status().isOk()).andReturn().getResponse();
        Cookie activeAccess = secondLogin.getCookie("access_token");
        Cookie secondRefresh = secondLogin.getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/change-password")
                        .cookie(activeAccess)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"password123\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));

        mockMvc.perform(get("/api/auth/me").cookie(activeAccess)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(secondRefresh)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ihsan", "password123"))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ihsan", "new-password"))))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_rotatesTokenAndRejectsReusingTheOldRefreshToken() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Cookie oldRefresh = registration.getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/refresh").cookie(oldRefresh))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refresh_token", org.hamcrest.Matchers.not(oldRefresh.getValue())));

        mockMvc.perform(post("/api/auth/refresh").cookie(oldRefresh))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withValidAccessToken_persistsBlacklistEntryAndClearsCookies() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Cookie access = registration.getCookie("access_token");
        Cookie refresh = registration.getCookie("refresh_token");

        mockMvc.perform(get("/api/cart").cookie(access)).andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/logout").cookie(access, refresh))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));
        String tokenId = jwtService.extractTokenId(access.getValue());
        assertThat(revokedAccessTokenRepository.existsById(tokenId)).isTrue();
        mockMvc.perform(get("/api/cart").cookie(access))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/cart"));
    }

    @Test
    void logout_revokesRefreshSessionIndependentlyFromAccessTokenBlacklist() throws Exception {
        var registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("ihsan", "ihsan@example.com", "password123"))))
                .andExpect(status().isCreated()).andReturn().getResponse();
        Cookie refresh = registration.getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/logout").cookie(refresh))
                .andExpect(status().isNoContent());

        assertThat(revokedAccessTokenRepository.count()).isZero();
        mockMvc.perform(post("/api/auth/refresh").cookie(refresh)).andExpect(status().isUnauthorized());
    }
}
