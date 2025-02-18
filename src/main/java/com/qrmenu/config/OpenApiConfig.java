package com.qrmenu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.examples.Example;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QR Menu API")
                        .description("API documentation for QR Menu Service\n\n" +
                                "API Versioning:\n" +
                                "- Current version: v1\n" +
                                "- Base path: /api/v1\n" +
                                "- Version is included in the URL path\n" +
                                "- Older versions will be marked as deprecated")
                        .version(ApiVersionConfig.CURRENT_API_VERSION)
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addExamples("loginRequest", createLoginRequestExample())
                        .addExamples("loginResponse", createLoginResponseExample())
                        .addExamples("refreshRequest", createRefreshRequestExample())
                        .addExamples("passwordResetRequest", createPasswordResetRequestExample())
                        .addExamples("passwordResetConfirm", createPasswordResetConfirmExample()));
    }

    private Example createLoginRequestExample() {
        return new Example()
                .value("""
                {
                    "email": "user@example.com",
                    "password": "Password123!"
                }
                """)
                .summary("Sample login request");
    }

    private Example createLoginResponseExample() {
        return new Example()
                .value("""
                {
                    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "refreshToken": "1234567890abcdef",
                    "expiresIn": 3600,
                    "tokenType": "Bearer"
                }
                """)
                .summary("Sample login response");
    }

    private Example createRefreshRequestExample() {
        return new Example()
                .value("""
                {
                    "refreshToken": "1234567890abcdef"
                }
                """)
                .summary("Sample refresh token request");
    }

    private Example createPasswordResetRequestExample() {
        return new Example()
                .value("""
                {
                    "email": "user@example.com"
                }
                """)
                .summary("Sample password reset request");
    }

    private Example createPasswordResetConfirmExample() {
        return new Example()
                .value("""
                {
                    "token": "1234567890abcdef",
                    "newPassword": "NewPassword123!"
                }
                """)
                .summary("Sample password reset confirmation");
    }
} 