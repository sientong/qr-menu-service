package com.qrmenu.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErrorResponseExamples {

    @Schema(name = "ValidationErrorExample")
    public static final String VALIDATION_ERROR = """
    {
        "timestamp": "2024-03-14T12:00:00Z",
        "status": 400,
        "error": "Bad Request",
        "message": "Validation failed",
        "path": "/api/v1/auth/login",
        "validationErrors": [
            {
                "field": "email",
                "message": "Invalid email format"
            },
            {
                "field": "password",
                "message": "Password is required"
            }
        ]
    }
    """;

    @Schema(name = "AuthenticationErrorExample")
    public static final String AUTH_ERROR = """
    {
        "timestamp": "2024-03-14T12:00:00Z",
        "status": 401,
        "error": "Unauthorized",
        "message": "Invalid credentials",
        "path": "/api/v1/auth/login"
    }
    """;

    @Schema(name = "RateLimitErrorExample")
    public static final String RATE_LIMIT_ERROR = """
    {
        "timestamp": "2024-03-14T12:00:00Z",
        "status": 429,
        "error": "Too Many Requests",
        "message": "Too many login attempts. Please try again later.",
        "path": "/api/v1/auth/login"
    }
    """;
} 