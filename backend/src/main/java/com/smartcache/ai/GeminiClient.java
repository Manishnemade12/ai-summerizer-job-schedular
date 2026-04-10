package com.smartcache.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcache.config.AppConfig;
import com.smartcache.model.SummarizeResult;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Gemini AI client — mirrors Go's ai/gemini.go + ai/prompt.go
 * Uses the Gemini REST API via OkHttp (same as the Go SDK does under the hood).
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private final AppConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(AppConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        if (config.getGeminiApiKey() == null || config.getGeminiApiKey().isEmpty()) {
            log.warn("WARNING: GEMINI_API_KEY is not set — AI summarization will fail");
        } else {
            log.info("✅ Gemini AI client initialized");
        }
    }

    /**
     * Builds the summarization prompt — mirrors Go's BuildPrompt function
     */
    public String buildPrompt(String text) {
        return "You are a concise summarization assistant.\n\n" +
                "Summarize the following text in 2-3 sentences and extract 2-4 relevant tags.\n\n" +
                "Return ONLY valid JSON in this exact format (no markdown, no code blocks):\n" +
                "{\n" +
                "  \"summary\": \"Your 2-3 sentence summary here.\",\n" +
                "  \"tags\": [\"tag1\", \"tag2\", \"tag3\"]\n" +
                "}\n\n" +
                "TEXT TO SUMMARIZE:\n" + text;
    }

    /**
     * Sends text to Gemini and returns a summary + tags.
     * Mirrors Go's Client.Summarize method.
     */
    @SuppressWarnings("unchecked")
    public SummarizeResult summarize(String text) throws IOException {
        String prompt = buildPrompt(text);

        // Build Gemini API request body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3
                )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + config.getGeminiApiKey())
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API error: HTTP " + response.code() +
                        " " + (response.body() != null ? response.body().string() : ""));
            }

            String responseBody = response.body().string();
            Map<String, Object> parsed = objectMapper.readValue(responseBody, Map.class);

            // Extract text from candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) parsed.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new IOException("Empty response from Gemini");
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new IOException("Empty response from Gemini");
            }

            String raw = (String) parts.get(0).get("text");

            // Clean markdown code blocks if present (same as Go)
            raw = raw.trim();
            if (raw.startsWith("```json")) raw = raw.substring(7);
            if (raw.startsWith("```")) raw = raw.substring(3);
            if (raw.endsWith("```")) raw = raw.substring(0, raw.length() - 3);
            raw = raw.trim();

            return objectMapper.readValue(raw, SummarizeResult.class);
        }
    }
}
