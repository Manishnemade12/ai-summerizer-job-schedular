package com.smartcache.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Job model — mirrors Go's worker.Job struct.
 * JSON field names match the Go struct tags exactly.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    @JsonProperty("job_id")
    private String id;

    @JsonProperty("input")
    private String input;

    @JsonProperty("status")
    private String status;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("error")
    private String error;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

    public Job() {}

    public Job(String id, String input, String status) {
        this.id = id;
        this.input = input;
        this.status = status;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}
