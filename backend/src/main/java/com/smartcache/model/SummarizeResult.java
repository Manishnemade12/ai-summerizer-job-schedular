package com.smartcache.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AI summarization result — mirrors Go's ai.SummarizeResult struct
 */
public class SummarizeResult {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("tags")
    private List<String> tags;

    public SummarizeResult() {}

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
