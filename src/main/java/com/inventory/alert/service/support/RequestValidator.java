package com.inventory.alert.service.support;

import com.inventory.alert.exception.InvalidRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw InvalidRequestException.fromViolations(violations);
        }
    }
}
