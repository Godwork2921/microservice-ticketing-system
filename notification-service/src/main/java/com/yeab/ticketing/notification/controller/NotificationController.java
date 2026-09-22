package com.yeab.ticketing.notification.controller;
import com.yeab.ticketing.notification.dto.*;
import com.yeab.ticketing.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/notifications") public class NotificationController {
 private final NotificationService service; public NotificationController(NotificationService service) { this.service = service; }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public NotificationResponse send(@Valid @RequestBody SendNotificationRequest request) { return service.send(request); }
 @GetMapping public Page<NotificationResponse> list(@RequestParam(required = false) String customerId, @PageableDefault(size = 20) Pageable pageable) { return service.list(customerId, pageable); }
 @GetMapping("/{id}") public NotificationResponse get(@PathVariable UUID id) { return service.getById(id); }
 @PostMapping("/{id}/retry") public NotificationResponse retry(@PathVariable UUID id) { return service.retry(id); }
}
