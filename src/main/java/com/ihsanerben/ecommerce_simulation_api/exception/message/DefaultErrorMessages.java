package com.ihsanerben.ecommerce_simulation_api.exception.message;

import java.util.List;

import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.ACCESS_DENIED;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.AUTHENTICATION_REQUIRED;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.BAD_CREDENTIALS;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.CONVERSATION_ALREADY_ASSIGNED;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.CSRF_TOKEN_INVALID;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.DATA_INTEGRITY_VIOLATION;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.DUPLICATE_RESOURCE;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.EMPTY_CART;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.ENDPOINT_NOT_FOUND;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.INSUFFICIENT_STOCK;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.INVALID_ORDER_STATE;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.INVALID_SORT_PROPERTY;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.INVALID_TOKEN;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.MAIL_FAILURE;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.MALFORMED_REQUEST_BODY;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.MEDIA_TYPE_NOT_SUPPORTED;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.METHOD_NOT_SUPPORTED;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.MISSING_PARAMETER;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.PASSWORD_REUSE;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.RATE_LIMIT_EXCEEDED;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.RESOURCE_NOT_FOUND;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.TYPE_MISMATCH;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.UNEXPECTED_ERROR;
import static com.ihsanerben.ecommerce_simulation_api.exception.message.ErrorMessageCodes.VALIDATION_FAILED;

public final class DefaultErrorMessages {

    private static final List<ResolvedErrorMessage> MESSAGES = List.of(
            message(RESOURCE_NOT_FOUND, "The requested resource was not found."),
            message(DUPLICATE_RESOURCE, "The resource already exists."),
            message(INSUFFICIENT_STOCK, "There is not enough stock for this product."),
            message(EMPTY_CART, "Cart is empty, cannot checkout."),
            message(INVALID_ORDER_STATE, "The order is not in a valid state for this operation."),
            message(CONVERSATION_ALREADY_ASSIGNED,
                    "This support conversation is already assigned to another agent."),
            message(INVALID_TOKEN, "The token is invalid or expired."),
            message(PASSWORD_REUSE, "The new password cannot be a recently used password."),
            message(RATE_LIMIT_EXCEEDED, "Too many requests. Please try again later."),
            message(MALFORMED_REQUEST_BODY, "Malformed request body."),
            message(TYPE_MISMATCH, "The request parameter has an invalid value."),
            message(INVALID_SORT_PROPERTY, "The requested sort property is invalid."),
            message(MISSING_PARAMETER, "A required request parameter is missing."),
            message(METHOD_NOT_SUPPORTED, "The HTTP method is not supported for this endpoint."),
            message(MEDIA_TYPE_NOT_SUPPORTED, "The request content type is not supported."),
            message(ENDPOINT_NOT_FOUND, "The requested endpoint was not found."),
            message(DATA_INTEGRITY_VIOLATION, "The operation conflicts with existing or related data."),
            message(MAIL_FAILURE, "Email service is temporarily unavailable. Please try again later."),
            message(BAD_CREDENTIALS, "Invalid username or password."),
            message(ACCESS_DENIED, "You do not have permission to perform this action."),
            message(VALIDATION_FAILED, "Validation failed."),
            message(UNEXPECTED_ERROR, "An unexpected error occurred."),
            message(AUTHENTICATION_REQUIRED, "Authentication is required to access this resource."),
            message(CSRF_TOKEN_INVALID,
                    "CSRF token is missing or invalid. Reload Swagger UI or initialize CSRF protection and retry."));

    private DefaultErrorMessages() {
    }

    public static List<ResolvedErrorMessage> all() {
        return MESSAGES;
    }

    private static ResolvedErrorMessage message(String code, String message) {
        return new ResolvedErrorMessage(code, message);
    }
}
