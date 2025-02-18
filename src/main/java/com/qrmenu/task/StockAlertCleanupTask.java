package com.qrmenu.task;

import com.qrmenu.config.StockManagementConfig;
import com.qrmenu.repository.StockAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StockAlertCleanupTask {
    private final StockAlertRepository alertRepository;
    private final StockManagementConfig config;

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public void cleanupOldAlerts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(config.getAlertCleanupDays());
        alertRepository.deleteByCreatedAtBeforeAndAcknowledgedAtIsNotNull(cutoff);
    }
} 