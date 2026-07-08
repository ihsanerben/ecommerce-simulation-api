package com.ihsanerben.ecommerce_simulation_api.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super("%s already exists with %s: '%s'".formatted(resourceName, fieldName, fieldValue));
    }
}
