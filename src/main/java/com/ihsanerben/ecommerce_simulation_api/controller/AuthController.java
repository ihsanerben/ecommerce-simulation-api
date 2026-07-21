package com.ihsanerben.ecommerce_simulation_api.controller;

import com.ihsanerben.ecommerce_simulation_api.dto.request.*;
import com.ihsanerben.ecommerce_simulation_api.dto.response.*;
import com.ihsanerben.ecommerce_simulation_api.exception.InvalidTokenException;
import com.ihsanerben.ecommerce_simulation_api.security.*;
import com.ihsanerben.ecommerce_simulation_api.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final TokenCookieService cookieService;

    @GetMapping("/csrf")
    @Operation(summary = "Initialize CSRF protection", description = "Run this once before POST, PUT, or DELETE operations in Swagger UI. Swagger then sends the CSRF header automatically.")
    @ApiResponse(responseCode = "204", description = "CSRF cookie initialized successfully")
    public ResponseEntity<Void> csrf(@Parameter(hidden = true) CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Operation(summary = "Register and sign in", description = "Creates a USER account and writes access/refresh JWTs as HttpOnly cookies. Tokens are not returned in the response body.")
    @ApiResponse(responseCode = "201", description = "Account created and authentication cookies written")
    @ApiResponse(responseCode = "400", description = "Validation error or malformed request")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        AuthResult result = authService.register(request, httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        write(response, result);
        return ResponseEntity.status(HttpStatus.CREATED).body(result.response());
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Creates a server-side refresh session and writes access/refresh JWTs as HttpOnly cookies.")
    @ApiResponse(responseCode = "200", description = "Login successful and authentication cookies written")
    @ApiResponse(responseCode = "400", description = "Validation error or malformed request")
    @ApiResponse(responseCode = "401", description = "Username or password is incorrect")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        AuthResult result = authService.login(request, httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        write(response, result);
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh token and issue a new token pair")
    @ApiResponse(responseCode = "200", description = "Token pair rotated and new cookies written")
    @ApiResponse(responseCode = "401", description = "Refresh cookie is missing, expired, revoked, or reused")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refresh = cookieService.read(request, TokenCookieService.REFRESH_COOKIE)
                .orElseThrow(() -> new InvalidTokenException("Refresh token cookie is missing."));
        AuthResult result = authService.refresh(refresh, request.getHeader("User-Agent"), request.getRemoteAddr());
        write(response, result);
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out from this session", description = "Revokes the refresh session, blacklists the current access token, and clears both authentication cookies.")
    @ApiResponse(responseCode = "204", description = "Session revoked and authentication cookies cleared")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(cookieService.read(request, TokenCookieService.REFRESH_COOKIE).orElse(null), accessToken(request));
        cookieService.clear(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Log out from every device", description = "Revokes all refresh sessions and increments tokenVersion so all older access tokens become invalid.")
    @ApiResponse(responseCode = "204", description = "All sessions revoked and authentication cookies cleared")
    @ApiResponse(responseCode = "401", description = "Authentication cookie is missing, expired, or revoked")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request, HttpServletResponse response) {
        authService.logoutAll(principal.getId(), accessToken(request));
        cookieService.clear(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email", description = "Always returns the same response to prevent account enumeration. Gmail SMTP must be configured to send the email.")
    @ApiResponse(responseCode = "200", description = "Password reset request accepted")
    @ApiResponse(responseCode = "400", description = "Email format is invalid")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    @ApiResponse(responseCode = "503", description = "Email service is temporarily unavailable")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok(new MessageResponse("If an account exists for this email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Consumes the one-time email token, rejects the last three passwords, and revokes all existing sessions.")
    @ApiResponse(responseCode = "200", description = "Password reset successfully; existing sessions revoked")
    @ApiResponse(responseCode = "400", description = "Validation error or malformed request")
    @ApiResponse(responseCode = "401", description = "Reset token is invalid, expired, or already used")
    @ApiResponse(responseCode = "403", description = "CSRF token is missing or invalid")
    @ApiResponse(responseCode = "409", description = "New password matches one of the last three passwords")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Password has been reset. Please log in again."));
    }

    private void write(HttpServletResponse response, AuthResult result) {
        cookieService.writeTokens(response, result.tokens().accessToken(), result.tokens().refreshToken());
    }

    private String accessToken(HttpServletRequest request) {
        return cookieService.read(request, TokenCookieService.ACCESS_COOKIE).orElseGet(() -> {
            String header = request.getHeader("Authorization");
            return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        });
    }
}
