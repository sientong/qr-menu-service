package com.qrmenu.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Password reset request payload")
public class PasswordResetRequest {
    @Schema(
        description = "Email address of the account to reset password",
        example = "user@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
} 