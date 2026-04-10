package com.smartcache.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcache.cache.RedisClient;
import com.smartcache.model.Job;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Status controller — mirrors Go's api/handlers/status.go
 * GET /api/status/{jobId}
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    private final RedisClient cache;
    private final ObjectMapper objectMapper;

    public StatusController(RedisClient cache) {
        this.cache = cache;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "job_id is required"));
        }

        String raw = cache.getJob(jobId);
        if (raw == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "job not found"));
        }

        try {
            Job job = objectMapper.readValue(raw, Job.class);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("job_id", job.getId());
            resp.put("status", job.getStatus());
            resp.put("created_at", job.getCreatedAt());

            if (Job.STATUS_COMPLETED.equals(job.getStatus())) {
                resp.put("summary", job.getSummary());
                resp.put("tags", job.getTags());
                resp.put("duration_ms", job.getDurationMs());
                resp.put("completed_at", job.getCompletedAt());
            }

            if (Job.STATUS_FAILED.equals(job.getStatus())) {
                resp.put("error", job.getError());
            }

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to parse job"));
        }
    }
}
