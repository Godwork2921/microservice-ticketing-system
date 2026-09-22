package com.yeab.ticketing.analytics.controller;
import com.yeab.ticketing.analytics.dto.*;
import com.yeab.ticketing.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/analytics") public class AnalyticsController {
 private final AnalyticsService service; public AnalyticsController(AnalyticsService service) { this.service = service; }
 @PostMapping("/events") @ResponseStatus(HttpStatus.CREATED) public Object ingest(@Valid @RequestBody AnalyticsEventRequest request) { return service.ingest(request); }
 @GetMapping("/summary") public List<AnalyticsSummary> summary() { return service.summary(); }
 @GetMapping("/overview") public AnalyticsOverview overview() { return service.overview(); }
}
