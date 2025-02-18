package com.qrmenu.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Refresh token request payload")
public class RefreshTokenRequest {
    @Schema(
        description = "Refresh token obtained during login",
        example = "1234567890abcdef"
    )
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
} 