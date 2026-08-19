package com.ihsanerben.ecommerce_simulation_api.security;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    private final ErrorMessageService errorMessageService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        boolean csrfFailure = exception instanceof MissingCsrfTokenException
                || exception instanceof InvalidCsrfTokenException;
        String code = csrfFailure ? ErrorMessageCodes.CSRF_TOKEN_INVALID : ErrorMessageCodes.ACCESS_DENIED;
        ResolvedErrorMessage errorMessage = errorMessageService.resolve(code);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(
                status.value(), status.getReasonPhrase(),
                errorMessage.code(), errorMessage.message(), request.getRequestURI()));
    }
}
