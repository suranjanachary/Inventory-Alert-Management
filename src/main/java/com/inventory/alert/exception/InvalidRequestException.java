package com.inventory.alert.exception;

import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

/**
 * Bean Validation failures when controllers are not yet present to run @Valid.
 */
public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public static InvalidRequestException fromViolations(Set<? extends ConstraintViolation<?>> violations) {
        String details = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return new InvalidRequestException("Validation failed: " + details);
    }
}
