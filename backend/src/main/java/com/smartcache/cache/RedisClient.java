package com.smartcache.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis/Valkey client — mirrors Go's cache/redis.go
 * Uses the same key prefixes and queue design.
 */
@Component
public class RedisClient {

    private static final Logger log = LoggerFactory.getLogger(RedisClient.class);

    public static final String KEY_PREFIX_SUMMARY = "summary:";
    public static final String KEY_PREFIX_JOB = "job:";
    public static final String KEY_JOB_QUEUE = "job_queue";
    public static final String KEY_METRIC_REQUESTS = "metrics:total_requests";
    public static final String KEY_METRIC_CACHE_HITS = "metrics:cache_hits";
    public static final String KEY_METRIC_CACHE_MISSES = "metrics:cache_misses";
    public static final String KEY_METRIC_FAILED = "metrics:failed";
    public static final String KEY_METRIC_TOTAL_TIME = "metrics:total_time_ms";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisClient(StringRedisTemplate redis) {
        this.redis = redis;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        try {
            redis.getConnectionFactory().getConnection().ping();
            log.info("✅ Connected to Valkey/Redis");
        } catch (Exception e) {
            log.error("❌ Failed to connect to Valkey: {}", e.getMessage());
        }
    }

    // --- Summary cache ---

    public String getSummary(String hash) {
        return redis.opsForValue().get(KEY_PREFIX_SUMMARY + hash);
    }

    public void setSummary(String hash, Object data, Duration ttl) throws Exception {
        String json = objectMapper.writeValueAsString(data);
        redis.opsForValue().set(KEY_PREFIX_SUMMARY + hash, json, ttl);
    }

    // --- Job storage ---

    public String getJob(String jobId) {
        return redis.opsForValue().get(KEY_PREFIX_JOB + jobId);
    }

    public void setJob(String jobId, Object job) throws Exception {
        String json = objectMapper.writeValueAsString(job);
        redis.opsForValue().set(KEY_PREFIX_JOB + jobId, json, Duration.ofHours(24));
    }

    // --- Queue ---

    public void pushQueue(String jobId) {
        redis.opsForList().rightPush(KEY_JOB_QUEUE, jobId);
    }

    /**
     * Blocking left-pop from the job queue.
     * Mirrors Go's BLPop with timeout 0 (block forever).
     * Uses a 5-second timeout to allow checking for shutdown.
     */
    public String popQueue() {
        // Use a 5-second timeout so the thread can check for shutdown signals
        String result = redis.opsForList().leftPop(KEY_JOB_QUEUE, 5, TimeUnit.SECONDS);
        return result;
    }

    public Long queueSize() {
        Long size = redis.opsForList().size(KEY_JOB_QUEUE);
        return size != null ? size : 0L;
    }

    // --- Metrics ---

    public void incrMetric(String key) {
        redis.opsForValue().increment(key);
    }

    public void addMetricTime(long ms) {
        redis.opsForValue().increment(KEY_METRIC_TOTAL_TIME, ms);
    }

    public long getMetricLong(String key) {
        String val = redis.opsForValue().get(key);
        if (val == null) return 0L;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
