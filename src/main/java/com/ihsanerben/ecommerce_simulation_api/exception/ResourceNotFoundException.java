package com.ihsanerben.ecommerce_simulation_api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("%s not found with %s: '%s'".formatted(resourceName, fieldName, fieldValue));
    }
}
