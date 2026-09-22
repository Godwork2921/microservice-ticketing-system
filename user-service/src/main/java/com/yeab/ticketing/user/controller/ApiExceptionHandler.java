package com.yeab.ticketing.user.controller;

import com.yeab.ticketing.common.error.ApiErrorResponse;
import com.yeab.ticketing.common.error.ErrorCode;
import com.yeab.ticketing.user.exception.EmailAlreadyRegisteredException;
import com.yeab.ticketing.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(UserNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> emailTaken(EmailAlreadyRegisteredException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ErrorCode.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> (FieldError) fieldError)
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.VALIDATION_FAILURE, details, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILURE,
                "Invalid value for '" + ex.getName() + "' (required type: " + ex.getRequiredType().getSimpleName() + ")", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED, "Request method is not allowed for this path", req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> noHandler(NoResourceFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "No handler found for this path", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error while processing {}", req.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred", req);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, ErrorCode code,
                                                   String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(status.value(), code, message, req.getRequestURI(), MDC.get("traceId")));
    }
}