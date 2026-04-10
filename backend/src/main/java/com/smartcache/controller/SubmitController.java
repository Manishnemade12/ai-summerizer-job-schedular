package com.smartcache.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcache.analytics.AnalyticsTracker;
import com.smartcache.cache.RedisClient;
import com.smartcache.model.Job;
import com.smartcache.model.SubmitRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Submit controller — mirrors Go's api/handlers/submit.go
 * POST /api/submit
 */
@RestController
@RequestMapping("/api")
public class SubmitController {

    private final RedisClient cache;
    private final AnalyticsTracker analytics;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubmitController(RedisClient cache, AnalyticsTracker analytics) {
        this.cache = cache;
        this.analytics = analytics;
    }

    @PostMapping("/submit")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody SubmitRequest req) {
        analytics.trackRequest();

        if (req.getInput() == null || req.getInput().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "input field is required"));
        }

        String input = req.getInput().trim();

        // Generate deterministic hash key (same as Go's hashInput)
        String hash = hashInput(input);

        // Check cache first (HIT path)
        String cached = cache.getSummary(hash);
        if (cached != null) {
            analytics.trackCacheHit();
            try {
                Map<String, Object> cachedData = objectMapper.readValue(cached, Map.class);
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("job_id", hash);
                resp.put("status", "completed");
                resp.put("cached", true);
                resp.put("summary", cachedData.get("summary"));
                resp.put("tags", cachedData.get("tags"));
                return ResponseEntity.ok(resp);
            } catch (Exception e) {
                // Cache parse error, treat as miss
            }
        }

        // MISS path — create job and enqueue
        analytics.trackCacheMiss();

        String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId, input, Job.STATUS_PENDING);

        try {
            cache.setJob(jobId, job);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to create job"));
        }

        cache.pushQueue(jobId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("job_id", jobId);
        resp.put("status", Job.STATUS_PENDING);
        resp.put("cached", false);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
    }

    /**
     * Generates a stable SHA-256 hash for deduplication — mirrors Go's hashInput.
     * Returns first 16 hex chars.
     */
    private String hashInput(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
