package com.yeab.ticketing.notification.controller;
import com.yeab.ticketing.notification.exception.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(NotificationNotFoundException.class) @ResponseStatus(HttpStatus.NOT_FOUND) Map<String,String> notFound(NotificationNotFoundException e) { return Map.of("error", e.getMessage()); }
}
