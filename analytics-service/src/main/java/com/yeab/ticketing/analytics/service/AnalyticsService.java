package com.yeab.ticketing.analytics.service;

import com.yeab.ticketing.analytics.dto.AnalyticsEventRequest;
import com.yeab.ticketing.analytics.dto.AnalyticsOverview;
import com.yeab.ticketing.analytics.dto.AnalyticsSummary;
import com.yeab.ticketing.analytics.entity.AnalyticsEventEntity;
import java.util.List;

public interface AnalyticsService {
    AnalyticsEventEntity ingest(AnalyticsEventRequest request);
    List<AnalyticsSummary> summary();
    AnalyticsOverview overview();
}
