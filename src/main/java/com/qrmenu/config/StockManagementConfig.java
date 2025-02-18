package com.qrmenu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.stock")
public class StockManagementConfig {
    private int lowStockThresholdDefault = 5;
    private long alertCleanupDays = 30;
    private boolean autoDisableOutOfStock = true;
} 