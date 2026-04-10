package com.smartcache.controller;

import com.smartcache.analytics.AnalyticsTracker;
import com.smartcache.model.Metrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Analytics controller — mirrors Go's api/handlers/analytics.go
 * GET /api/analytics
 */
@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsTracker tracker;

    public AnalyticsController(AnalyticsTracker tracker) {
        this.tracker = tracker;
    }

    @GetMapping("/analytics")
    public ResponseEntity<Metrics> getAnalytics() {
        return ResponseEntity.ok(tracker.getMetrics());
    }
}
