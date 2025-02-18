package com.qrmenu.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Password reset confirmation payload")
public class PasswordResetConfirmRequest {
    @Schema(
        description = "Password reset token received via email",
        example = "1234567890abcdef"
    )
    @NotBlank(message = "Token is required")
    private String token;

    @Schema(
        description = "New password",
        example = "NewPassword123!",
        pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one digit, " +
                "one lowercase letter, one uppercase letter, and one special character"
    )
    private String newPassword;
} 