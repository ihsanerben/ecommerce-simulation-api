package com.ihsanerben.ecommerce_simulation_api.exception.message;

public final class ErrorMessageCodes {

    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String EMPTY_CART = "EMPTY_CART";
    public static final String INVALID_ORDER_STATE = "INVALID_ORDER_STATE";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String PASSWORD_REUSE = "PASSWORD_REUSE";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String MALFORMED_REQUEST_BODY = "MALFORMED_REQUEST_BODY";
    public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
    public static final String INVALID_SORT_PROPERTY = "INVALID_SORT_PROPERTY";
    public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    public static final String METHOD_NOT_SUPPORTED = "METHOD_NOT_SUPPORTED";
    public static final String MEDIA_TYPE_NOT_SUPPORTED = "MEDIA_TYPE_NOT_SUPPORTED";
    public static final String ENDPOINT_NOT_FOUND = "ENDPOINT_NOT_FOUND";
    public static final String DATA_INTEGRITY_VIOLATION = "DATA_INTEGRITY_VIOLATION";
    public static final String MAIL_FAILURE = "MAIL_FAILURE";
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
    public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
    public static final String CSRF_TOKEN_INVALID = "CSRF_TOKEN_INVALID";

    private ErrorMessageCodes() {
    }
}
