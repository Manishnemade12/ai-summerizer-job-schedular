package com.smartcache.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration — mirrors Go's config/config.go
 */
@Configuration
public class AppConfig {

    @Value("${server.port:8080}")
    private int port;

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${worker.count:3}")
    private int workerCount;

    @Value("${cache.ttl:300}")
    private int cacheTtl;

    public int getPort() { return port; }
    public String getGeminiApiKey() { return geminiApiKey; }
    public int getWorkerCount() { return workerCount; }
    public int getCacheTtl() { return cacheTtl; }
}
