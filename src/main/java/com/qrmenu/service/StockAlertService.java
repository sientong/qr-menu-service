package com.qrmenu.service;

import com.qrmenu.dto.stock.StockAlertResponse;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.StockAlert;
import com.qrmenu.model.StockAlertType;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.repository.StockAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockAlertService {
    private final StockAlertRepository alertRepository;
    private final UserService userService;

    @Transactional
    public void createLowStockAlert(MenuItem item) {
        if (item.getStockQuantity() <= item.getLowStockThreshold()) {
            createAlert(item, StockAlertType.LOW_STOCK,
                    String.format("Low stock alert: %s has only %d units remaining",
                            item.getName(), item.getStockQuantity()));
        }
    }

    @Transactional
    public void createOutOfStockAlert(MenuItem item) {
        if (item.getStockQuantity() <= 0) {
            createAlert(item, StockAlertType.OUT_OF_STOCK,
                    String.format("Out of stock alert: %s is out of stock",
                            item.getName()));
        }
    }

    @Transactional
    public void createValuationChangeAlert(MenuItem item, String message) {
        createAlert(item, StockAlertType.VALUATION_CHANGE, message);
    }

    @Transactional
    public void acknowledgeAlert(Long alertId) {
        StockAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(userService.getCurrentUser().getEmail());
        alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> getActiveAlerts() {
        Long restaurantId = userService.getCurrentUser().getRestaurant().getId();
        return alertRepository.findByMenuItemCategoryRestaurantIdAndAcknowledgedAtIsNull(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void createAlert(MenuItem item, StockAlertType type, String message) {
        StockAlert alert = StockAlert.builder()
                .menuItem(item)
                .alertType(type)
                .message(message)
                .build();
        alertRepository.save(alert);
    }

    private StockAlertResponse mapToResponse(StockAlert alert) {
        return StockAlertResponse.builder()
                .id(alert.getId())
                .menuItemId(alert.getMenuItem().getId())
                .menuItemName(alert.getMenuItem().getName())
                .alertType(alert.getAlertType())
                .message(alert.getMessage())
                .createdAt(alert.getCreatedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .acknowledgedBy(alert.getAcknowledgedBy())
                .build();
    }
} 