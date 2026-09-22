package com.yeab.ticketing.reservation.controller;

import com.yeab.ticketing.common.error.ApiErrorResponse;
import com.yeab.ticketing.common.error.ErrorCode;
import com.yeab.ticketing.reservation.exception.DiscountNotApplicableException;
import com.yeab.ticketing.reservation.exception.EventNotAvailableException;
import com.yeab.ticketing.reservation.exception.EventNotBookableException;
import com.yeab.ticketing.reservation.exception.InvalidReservationStateException;
import com.yeab.ticketing.reservation.exception.ReservationExpiredException;
import com.yeab.ticketing.reservation.exception.ReservationNotFoundException;
import com.yeab.ticketing.reservation.exception.SeatAlreadyReservedException;
import com.yeab.ticketing.reservation.exception.ServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
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

/**
 * Translates domain failures into the shared {@link ApiErrorResponse} contract so
 * the gateway, analytics, and the UI all see the same error shape.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(ReservationNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ErrorCode.RESERVATION_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(EventNotAvailableException.class)
    public ResponseEntity<ApiErrorResponse> eventUnavailable(EventNotAvailableException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_AVAILABLE, ex.getMessage(), req);
    }

    @ExceptionHandler(SeatAlreadyReservedException.class)
    public ResponseEntity<ApiErrorResponse> seatTaken(SeatAlreadyReservedException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ErrorCode.SEAT_ALREADY_RESERVED, ex.getMessage(), req);
    }

    @ExceptionHandler(ReservationExpiredException.class)
    public ResponseEntity<ApiErrorResponse> expired(ReservationExpiredException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ErrorCode.RESERVATION_EXPIRED, ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidReservationStateException.class)
    public ResponseEntity<ApiErrorResponse> invalidState(InvalidReservationStateException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ErrorCode.INVALID_RESERVATION_STATE, ex.getMessage(), req);
    }

    @ExceptionHandler({EventNotBookableException.class, DiscountNotApplicableException.class})
    public ResponseEntity<ApiErrorResponse> unprocessable(RuntimeException ex, HttpServletRequest req) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.INVALID_RESERVATION_STATE, ex.getMessage(), req);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> unavailable(ServiceUnavailableException ex, HttpServletRequest req) {
        log.warn("Downstream service unavailable: {}", ex.getMessage());
        return error(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE,
                "A required service is temporarily unavailable", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> (FieldError) fieldError)
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.VALIDATION_FAILURE, details, req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Request conflicts with current data", req);
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
        String traceId = MDC.get("traceId");
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(status.value(), code, message, req.getRequestURI(), traceId));
    }
}