package com.ihsanerben.ecommerce_simulation_api.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(String message) {
        super(message);
    }
}
