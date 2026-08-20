package com.ihsanerben.ecommerce_simulation_api.exception;

import com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes;
import com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageService;
import com.ihsanerben.ecommerce_simulation_api.exception.message.ResolvedErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.ihsanerben.ecommerce_simulation_api.support.exception.ConversationAlreadyAssignedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorMessageService errorMessageService;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ErrorMessageCodes.RESOURCE_NOT_FOUND, request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorMessageCodes.DUPLICATE_RESOURCE, request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorMessageCodes.INSUFFICIENT_STOCK, request);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse> handleEmptyCart(EmptyCartException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorMessageCodes.EMPTY_CART, request);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderState(InvalidOrderStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorMessageCodes.INVALID_ORDER_STATE, request);
    }

    @ExceptionHandler(ConversationAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleConversationAlreadyAssigned(
            ConversationAlreadyAssignedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorMessageCodes.CONVERSATION_ALREADY_ASSIGNED, request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorMessageCodes.INVALID_TOKEN, request);
    }

    @ExceptionHandler(PasswordReuseException.class)
    public ResponseEntity<ErrorResponse> handlePasswordReuse(PasswordReuseException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorMessageCodes.PASSWORD_REUSE, request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex,
            HttpServletRequest request) {
        ResolvedErrorMessage errorMessage = errorMessageService.resolve(ErrorMessageCodes.RATE_LIMIT_EXCEEDED);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                errorMessage.code(),
                errorMessage.message(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequestBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorMessageCodes.MALFORMED_REQUEST_BODY, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorMessageCodes.TYPE_MISMATCH, request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortProperty(PropertyReferenceException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorMessageCodes.INVALID_SORT_PROPERTY, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorMessageCodes.MISSING_PARAMETER, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorMessageCodes.METHOD_NOT_SUPPORTED, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorMessageCodes.MEDIA_TYPE_NOT_SUPPORTED, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ErrorMessageCodes.ENDPOINT_NOT_FOUND, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ErrorMessageCodes.DATA_INTEGRITY_VIOLATION, request);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse> handleMailFailure(MailException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ErrorMessageCodes.MAIL_FAILURE, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorMessageCodes.BAD_CREDENTIALS, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorMessageCodes.ACCESS_DENIED, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ResolvedErrorMessage errorMessage = errorMessageService.resolve(ErrorMessageCodes.VALIDATION_FAILED);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorMessage.code(),
                errorMessage.message(),
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageCodes.UNEXPECTED_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, HttpServletRequest request) {
        ResolvedErrorMessage errorMessage = errorMessageService.resolve(code);
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                errorMessage.code(),
                errorMessage.message(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
