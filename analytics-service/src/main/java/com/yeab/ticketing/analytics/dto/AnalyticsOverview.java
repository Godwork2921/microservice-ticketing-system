package com.yeab.ticketing.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsOverview(List<AnalyticsSummary> byEventType,
                                BigDecimal totalRevenue,
                                long confirmedReservations,
                                long ticketsGenerated) { }