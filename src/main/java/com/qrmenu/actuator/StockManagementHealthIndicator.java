package com.qrmenu.actuator;

import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.StockAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockManagementHealthIndicator implements HealthIndicator {
    private final MenuItemRepository menuItemRepository;
    private final StockAlertRepository alertRepository;

    @Override
    public Health health() {
        try {
            long outOfStockCount = menuItemRepository.countByTrackStockTrueAndStockQuantityLessThanEqual(0);
            long unacknowledgedAlerts = alertRepository.countByAcknowledgedAtIsNull();

            return Health.up()
                    .withDetail("outOfStockItems", outOfStockCount)
                    .withDetail("unacknowledgedAlerts", unacknowledgedAlerts)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
} 