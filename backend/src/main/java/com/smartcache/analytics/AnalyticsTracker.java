package com.smartcache.analytics;

import com.smartcache.cache.RedisClient;
import com.smartcache.model.Metrics;
import org.springframework.stereotype.Component;

/**
 * Analytics tracker — mirrors Go's analytics/metrics.go
 * Tracks requests, cache hits/misses, failures, and processing times via Redis.
 */
@Component
public class AnalyticsTracker {

    private final RedisClient cache;

    public AnalyticsTracker(RedisClient cache) {
        this.cache = cache;
    }

    public void trackRequest() {
        cache.incrMetric(RedisClient.KEY_METRIC_REQUESTS);
    }

    public void trackCacheHit() {
        cache.incrMetric(RedisClient.KEY_METRIC_CACHE_HITS);
    }

    public void trackCacheMiss() {
        cache.incrMetric(RedisClient.KEY_METRIC_CACHE_MISSES);
    }

    public void trackFailure() {
        cache.incrMetric(RedisClient.KEY_METRIC_FAILED);
    }

    public void trackProcessingTime(long ms) {
        cache.addMetricTime(ms);
    }

    /**
     * Retrieves all current analytics — mirrors Go's Tracker.GetMetrics
     */
    public Metrics getMetrics() {
        long total = cache.getMetricLong(RedisClient.KEY_METRIC_REQUESTS);
        long hits = cache.getMetricLong(RedisClient.KEY_METRIC_CACHE_HITS);
        long misses = cache.getMetricLong(RedisClient.KEY_METRIC_CACHE_MISSES);
        long failed = cache.getMetricLong(RedisClient.KEY_METRIC_FAILED);
        long totalTime = cache.getMetricLong(RedisClient.KEY_METRIC_TOTAL_TIME);
        long queueSize = cache.queueSize();

        double avgTime = 0;
        if (misses > 0) {
            avgTime = (double) totalTime / misses;
        }

        return new Metrics(total, hits, misses, failed, queueSize, avgTime);
    }
}
