package com.ihsanerben.ecommerce_simulation_api.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.exception.ErrorResponse;
import com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes;
import com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageService;
import com.ihsanerben.ecommerce_simulation_api.exception.message.ResolvedErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final ErrorMessageService errorMessageService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResolvedErrorMessage errorMessage = errorMessageService.resolve(ErrorMessageCodes.AUTHENTICATION_REQUIRED);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(
                status.value(), status.getReasonPhrase(),
                errorMessage.code(), errorMessage.message(), request.getRequestURI()));
    }
}
