package com.inventory.alert.exception;

import com.inventory.alert.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, ErrorCodes.PRODUCT_NOT_FOUND, null);
    }

    @ExceptionHandler(AlertNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAlertNotFound(
            AlertNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, ErrorCodes.ALERT_NOT_FOUND, null);
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSku(
            DuplicateSkuException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, ErrorCodes.DUPLICATE_SKU, null);
    }

    @ExceptionHandler(DuplicateAlertException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAlert(
            DuplicateAlertException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, ErrorCodes.DUPLICATE_ALERT, null);
    }

    @ExceptionHandler(AlertAlreadyResolvedException.class)
    public ResponseEntity<ErrorResponse> handleAlertAlreadyResolved(
            AlertAlreadyResolvedException ex, HttpServletRequest request) {
        return build(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request,
                ErrorCodes.ALERT_ALREADY_RESOLVED,
                null);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {
        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                ex.getMessage(),
                request,
                ErrorCodes.INSUFFICIENT_STOCK,
                null);
    }

    @ExceptionHandler(InactiveProductException.class)
    public ResponseEntity<ErrorResponse> handleInactiveProduct(
            InactiveProductException ex, HttpServletRequest request) {
        return build(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                ex.getMessage(),
                request,
                ErrorCodes.INACTIVE_PRODUCT,
                null);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                ex.getMessage(),
                request,
                ErrorCodes.VALIDATION_FAILED,
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Request validation failed",
                request,
                ErrorCodes.VALIDATION_FAILED,
                fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> ErrorResponse.FieldErrorDetail.builder()
                        .field(v.getPropertyPath().toString())
                        .message(v.getMessage())
                        .rejectedValue(v.getInvalidValue())
                        .build())
                .toList();
        return build(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Constraint validation failed",
                request,
                ErrorCodes.VALIDATION_FAILED,
                fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Malformed or unreadable request body",
                request,
                ErrorCodes.MALFORMED_REQUEST,
                null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic lock conflict on {}", request.getRequestURI());
        return build(
                HttpStatus.CONFLICT,
                "Conflict",
                "The resource was modified concurrently. Retry the operation.",
                request,
                ErrorCodes.OPTIMISTIC_LOCK,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {}", request.getRequestURI(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request,
                ErrorCodes.INTERNAL_ERROR,
                null);
    }

    private ErrorResponse.FieldErrorDetail toFieldError(FieldError fieldError) {
        return ErrorResponse.FieldErrorDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            String errorCode,
            List<ErrorResponse.FieldErrorDetail> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .errorCode(errorCode)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
