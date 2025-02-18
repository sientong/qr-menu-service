package com.qrmenu.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.qrmenu.config.ApiVersion;
import com.qrmenu.config.ApiVersionConfig;

@Data
@Schema(
    description = "Login request payload",
    requiredProperties = {"email", "password"}
)
@ApiVersion(ApiVersionConfig.CURRENT_API_VERSION)
public class LoginRequest {
    @Schema(
        description = "User's email address",
        example = "user@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(
        description = "User's password",
        example = "Password123!",
        minLength = 8
    )
    @NotBlank(message = "Password is required")
    private String password;
} 