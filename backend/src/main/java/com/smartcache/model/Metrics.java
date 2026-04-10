package com.smartcache.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Analytics response — mirrors Go's analytics.Metrics struct
 */
public class Metrics {

    @JsonProperty("total_requests")
    private long totalRequests;

    @JsonProperty("cache_hits")
    private long cacheHits;

    @JsonProperty("cache_misses")
    private long cacheMisses;

    @JsonProperty("failed_jobs")
    private long failedJobs;

    @JsonProperty("queue_size")
    private long queueSize;

    @JsonProperty("avg_processing_time_ms")
    private double avgProcessingTimeMs;

    public Metrics() {}

    public Metrics(long totalRequests, long cacheHits, long cacheMisses,
                   long failedJobs, long queueSize, double avgProcessingTimeMs) {
        this.totalRequests = totalRequests;
        this.cacheHits = cacheHits;
        this.cacheMisses = cacheMisses;
        this.failedJobs = failedJobs;
        this.queueSize = queueSize;
        this.avgProcessingTimeMs = avgProcessingTimeMs;
    }

    // Getters
    public long getTotalRequests() { return totalRequests; }
    public long getCacheHits() { return cacheHits; }
    public long getCacheMisses() { return cacheMisses; }
    public long getFailedJobs() { return failedJobs; }
    public long getQueueSize() { return queueSize; }
    public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
}
