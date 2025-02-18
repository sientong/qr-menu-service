package com.qrmenu.controller;

import com.qrmenu.dto.auth.LoginRequest;
import com.qrmenu.dto.auth.RefreshTokenRequest;
import com.qrmenu.dto.auth.TokenResponse;
import com.qrmenu.dto.auth.PasswordResetRequest;
import com.qrmenu.dto.auth.PasswordResetConfirmRequest;
import com.qrmenu.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.qrmenu.config.ApiVersionConfig;
import com.qrmenu.exception.AuthenticationException;
import com.qrmenu.model.User;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Version",
    description = "API Version: " + ApiVersionConfig.CURRENT_API_VERSION
)
public class AuthController {

    private final AuthenticationService authService;

    @Operation(
        summary = "Login with email and password",
        description = "Authenticates a user and returns access and refresh tokens"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many login attempts"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Login credentials",
                required = true
            )
            @Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Refresh access token",
        description = "Gets a new access token using a refresh token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token successfully refreshed",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid refresh token"
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Logout",
        description = "Invalidates the current access token",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully logged out"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid token"
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Bearer token", required = true)
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Logout from all devices",
        description = "Invalidates all access tokens for the current user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully logged out from all devices"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid token"
        )
    })
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @Parameter(description = "Bearer token", required = true)
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        User currentUser = authService.getCurrentUser(token);
        authService.logoutAll(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Request password reset",
        description = "Sends a password reset email to the user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email sent successfully"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many password reset attempts"
        )
    })
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Confirm password reset",
        description = "Resets the user's password using a reset token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Password successfully reset"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired reset token"
        )
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new AuthenticationException("Invalid authorization header");
    }
} 