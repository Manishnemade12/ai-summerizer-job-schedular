package com.smartcache.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for POST /api/submit — mirrors Go's submitRequest struct
 */
public class SubmitRequest {

    @JsonProperty("input")
    private String input;

    public SubmitRequest() {}

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}
