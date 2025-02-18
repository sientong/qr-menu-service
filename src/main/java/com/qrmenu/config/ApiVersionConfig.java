package com.qrmenu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class ApiVersionConfig implements WebMvcConfigurer {
    public static final String API_V1 = "v1";
    public static final String CURRENT_API_VERSION = "1.0";
    public static final String DEPRECATED_API_VERSION = "deprecated";
} 