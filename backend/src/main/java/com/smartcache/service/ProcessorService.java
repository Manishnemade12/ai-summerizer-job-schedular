package com.smartcache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcache.ai.GeminiClient;
import com.smartcache.analytics.AnalyticsTracker;
import com.smartcache.cache.RedisClient;
import com.smartcache.model.SummarizeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Processor service — mirrors Go's services/processor.go
 * Orchestrates: resolve input → call AI → cache result
 */
@Service
public class ProcessorService {

    private static final Logger log = LoggerFactory.getLogger(ProcessorService.class);

    private final RedisClient cache;
    private final GeminiClient aiClient;
    private final AnalyticsTracker analytics;
    private final Duration cacheTtl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcessorService(RedisClient cache, GeminiClient aiClient,
                            AnalyticsTracker analytics,
                            @Value("${cache.ttl:300}") int cacheTtlSeconds) {
        this.cache = cache;
        this.aiClient = aiClient;
        this.analytics = analytics;
        this.cacheTtl = Duration.ofSeconds(cacheTtlSeconds);
    }

    /**
     * Result of processing a job
     */
    public static class ProcessResult {
        private final String summary;
        private final java.util.List<String> tags;
        private final long durationMs;

        public ProcessResult(String summary, java.util.List<String> tags, long durationMs) {
            this.summary = summary;
            this.tags = tags;
            this.durationMs = durationMs;
        }

        public String getSummary() { return summary; }
        public java.util.List<String> getTags() { return tags; }
        public long getDurationMs() { return durationMs; }
    }

    /**
     * Process handles one job: fetch (if URL), call AI, cache result.
     * Mirrors Go's Processor.Process method.
     */
    public ProcessResult process(String jobId, String input) throws Exception {
        long start = System.currentTimeMillis();

        // Resolve text content (plain text or URL)
        String text = resolveInput(input);

        // Call Gemini AI
        SummarizeResult result;
        try {
            result = aiClient.summarize(text);
        } catch (Exception e) {
            analytics.trackFailure();
            throw new RuntimeException("AI summarization failed: " + e.getMessage(), e);
        }

        long elapsed = System.currentTimeMillis() - start;
        analytics.trackProcessingTime(elapsed);

        // Cache the result
        try {
            cache.setSummary(jobId, result, cacheTtl);
        } catch (Exception e) {
            log.warn("Warning: failed to cache result for job {}: {}", jobId, e.getMessage());
        }

        return new ProcessResult(result.getSummary(), result.getTags(), elapsed);
    }

    /**
     * Resolves input — plain text is returned as-is, URLs are fetched.
     * Mirrors Go's resolveInput function.
     */
    private String resolveInput(String input) throws Exception {
        input = input.trim();
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return fetchUrl(input);
        }
        return input;
    }

    /**
     * Fetches text content from a URL — mirrors Go's fetchURL function.
     */
    private String fetchUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String body = reader.lines().collect(Collectors.joining("\n"));

            // Limit body size to 50KB
            if (body.length() > 50_000) {
                body = body.substring(0, 50_000);
            }

            String text = stripHtml(body);

            // Limit text to 8000 chars (same as Go)
            if (text.length() > 8000) {
                text = text.substring(0, 8000);
            }

            return text;
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Strips HTML tags — mirrors Go's stripHTML function.
     */
    private String stripHtml(String html) {
        StringBuilder result = new StringBuilder();
        boolean inTag = false;
        for (char c : html.toCharArray()) {
            if (c == '<') {
                inTag = true;
            } else if (c == '>') {
                inTag = false;
                result.append(' ');
            } else if (!inTag) {
                result.append(c);
            }
        }
        // Collapse whitespace (same as Go's strings.Fields + Join)
        return result.toString().trim().replaceAll("\\s+", " ");
    }
}
