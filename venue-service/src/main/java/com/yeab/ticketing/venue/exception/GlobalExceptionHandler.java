package com.yeab.ticketing.venue.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(VenueNotFoundException.class)
    ResponseEntity<ApiError> notFound(VenueNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(SeatNotFoundException.class)
    ResponseEntity<ApiError> seatNotFound(SeatNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler({InvalidSeatAttributesException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> invalid(RuntimeException exception, HttpServletRequest request) {
        String code = exception instanceof InvalidSeatAttributesException
                ? "INVALID_SEAT_ATTRIBUTES" : "INVALID_VENUE_REQUEST";
        return error(HttpStatus.UNPROCESSABLE_ENTITY, code,
            exception.getMessage() == null ? "The venue request is invalid." : exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> duplicate(DataIntegrityViolationException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_SEAT", "The seat position already exists.", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "Request body is invalid or malformed. Provide a JSON array of seats for bulk import.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request validation failed.");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraint(ConstraintViolationException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Invalid value for '" + exception.getName() + "'", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Request method is not allowed for this path.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> noHandler(NoResourceFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "No handler found for this path.", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code,
                message == null ? "The request is invalid." : message, request.getRequestURI(), traceId));
    }
}
