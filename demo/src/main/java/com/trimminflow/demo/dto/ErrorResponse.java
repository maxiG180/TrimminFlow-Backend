package com.trimminflow.demo.dto;

/**
 * ErrorResponse DTO
 *
 * Standard error response format for API errors
 */
public class ErrorResponse {

    private String error;
    private String message;

    public ErrorResponse() {}

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
