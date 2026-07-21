package com.ihsanerben.ecommerce_simulation_api.exception;

public class PasswordReuseException extends RuntimeException {
    public PasswordReuseException(String message) { super(message); }
}
