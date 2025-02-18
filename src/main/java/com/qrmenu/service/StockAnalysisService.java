package com.qrmenu.service;

import com.qrmenu.dto.stock.StockTrendResponse;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.StockHistory;
import com.qrmenu.model.User;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing stock trends and patterns.
 * Provides insights into stock movement and usage patterns over time.
 */
@Service
@RequiredArgsConstructor
public class StockAnalysisService {
    private final MenuItemRepository menuItemRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final UserService userService;

    /**
     * Analyzes stock trends for all items in a restaurant over a specified period.
     *
     * @param restaurantId ID of the restaurant
     * @param days Number of days to analyze
     * @return List of stock trends including average, min, and max quantities
     * @throws IllegalArgumentException if parameters are invalid
     * @throws AccessDeniedException if user doesn't have access to the restaurant
     */
    @Transactional(readOnly = true)
    public List<StockTrendResponse> analyzeStockTrends(Long restaurantId, int days) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        if (days > 365) {
            throw new IllegalArgumentException("Analysis period cannot exceed one year");
        }

        validateRestaurantAccess(restaurantId);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<StockHistory> history = stockHistoryRepository
                .findByRestaurantIdAndDateRange(restaurantId, startDate, LocalDateTime.now());

        if (history.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<StockHistory>> historyByItem = history.stream()
                .collect(Collectors.groupingBy(h -> h.getMenuItem().getId()));

        return historyByItem.entrySet().stream()
                .map(entry -> calculateTrend(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private StockTrendResponse calculateTrend(Long menuItemId, List<StockHistory> history) {
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("History cannot be null or empty");
        }

        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        double averageQuantity = history.stream()
                .mapToInt(StockHistory::getNewQuantity)
                .average()
                .orElse(0.0);

        int maxQuantity = history.stream()
                .mapToInt(StockHistory::getNewQuantity)
                .max()
                .orElse(0);

        int minQuantity = history.stream()
                .mapToInt(StockHistory::getNewQuantity)
                .min()
                .orElse(0);

        return StockTrendResponse.builder()
                .menuItemId(menuItemId)
                .menuItemName(item.getName())
                .currentQuantity(item.getStockQuantity())
                .averageQuantity(averageQuantity)
                .maxQuantity(maxQuantity)
                .minQuantity(minQuantity)
                .adjustmentCount(history.size())
                .build();
    }

    private void validateRestaurantAccess(Long restaurantId) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            throw new AccessDeniedException("Unauthorized access to restaurant data");
        }
    }
} 