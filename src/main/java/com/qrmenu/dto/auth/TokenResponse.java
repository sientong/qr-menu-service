package com.qrmenu.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Authentication token response")
public class TokenResponse {
    @Schema(
        description = "JWT access token",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
        description = "Refresh token for obtaining new access tokens",
        example = "1234567890abcdef"
    )
    private String refreshToken;

    @Schema(
        description = "Access token expiration time in seconds",
        example = "3600"
    )
    private long expiresIn;

    @Schema(
        description = "Type of token",
        example = "Bearer"
    )
    private String tokenType;
} 