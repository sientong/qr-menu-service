package com.qrmenu.service;

import com.qrmenu.dto.stock.BatchValuationRequest;
import com.qrmenu.dto.stock.StockValuationResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.exception.StockValidationException;
import com.qrmenu.model.User;
import com.qrmenu.model.MenuItem;
import com.qrmenu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing stock valuations of menu items.
 * Handles the calculation and updating of stock values, and generates alerts for significant changes.
 */
@Service
@RequiredArgsConstructor
public class StockValuationService {
    private final MenuItemRepository menuItemRepository;
    private final StockAlertService stockAlertService;
    private final UserService userService;

    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    @Transactional(readOnly = true)
    public List<StockValuationResponse> getValuations(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }

        validateRestaurantAccess(restaurantId);

        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .filter(MenuItem::isTrackStock)
                .map(this::mapToValuationResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<StockValuationResponse> updateValuations(BatchValuationRequest request) {
        validateValuationRequest(request);

        List<Long> itemIds = request.getUpdates().stream()
                .map(BatchValuationRequest.ValuationUpdate::getMenuItemId)
                .collect(Collectors.toList());

        List<MenuItem> items = menuItemRepository.findAllById(itemIds);
        
        // Check for missing items with detailed error message
        if (items.size() != itemIds.size()) {
            Set<Long> foundIds = items.stream()
                    .map(MenuItem::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = itemIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Menu items not found: " + missingIds);
        }

        validateItemsOwnership(items);

        // Validate unit costs and update items
        Map<Long, BigDecimal> costUpdates = request.getUpdates().stream()
                .collect(Collectors.toMap(
                    BatchValuationRequest.ValuationUpdate::getMenuItemId,
                    BatchValuationRequest.ValuationUpdate::getUnitCost
                ));

        items.forEach(item -> {
            BigDecimal newUnitCost = costUpdates.get(item.getId());
            
            if (newUnitCost == null || newUnitCost.compareTo(BigDecimal.ZERO) <= 0) {
                throw new StockValidationException("Invalid unit cost for item " + item.getName() + ": must be greater than zero");
            }
            
            updateItemValuation(item, newUnitCost);
        });

        List<MenuItem> savedItems = menuItemRepository.saveAll(items);
        return savedItems.stream()
                .map(this::mapToValuationResponse)
                .collect(Collectors.toList());
    }

    private void updateItemValuation(MenuItem item, BigDecimal newUnitCost) {
        if (!item.isTrackStock()) {
            throw new StockValidationException("Stock tracking is not enabled for this item");
        }

        if (item.getUnitCost() != null && !item.getUnitCost().equals(newUnitCost)) {
            BigDecimal changePercent = newUnitCost.subtract(item.getUnitCost())
                    .divide(item.getUnitCost())
                    .setScale(2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (changePercent.abs().compareTo(BigDecimal.TEN) > 0) {
                stockAlertService.createValuationChangeAlert(item,
                        String.format("Unit cost for %s changed by %.2f%% (from %s to %s)",
                                item.getName(), changePercent, item.getUnitCost(), newUnitCost));
            }
        }

        item.setUnitCost(newUnitCost);
        item.setLastValuationDate(LocalDateTime.now());
    }

    private StockValuationResponse mapToValuationResponse(MenuItem item) {
        BigDecimal totalValue = item.getUnitCost() != null && item.getStockQuantity() != null
                ? item.getUnitCost().multiply(BigDecimal.valueOf(item.getStockQuantity()))
                : BigDecimal.ZERO;

        return StockValuationResponse.builder()
                .menuItemId(item.getId())
                .menuItemName(item.getName())
                .stockQuantity(item.getStockQuantity())
                .unitCost(item.getUnitCost())
                .totalValue(totalValue)
                .lastValuationDate(item.getLastValuationDate())
                .build();
    }

    private void validateValuationRequest(BatchValuationRequest request) {
        if (request == null || request.getUpdates() == null || request.getUpdates().isEmpty()) {
            throw new StockValidationException("Valuation updates cannot be empty");
        }

        if (request.getUpdates().size() > 100) {
            throw new StockValidationException("Cannot update more than 100 items at once");
        }

        request.getUpdates().forEach(update -> {
            if (update.getMenuItemId() == null) {
                throw new StockValidationException("Menu item ID cannot be null");
            }

            if (update.getUnitCost() == null) {
                throw new StockValidationException("Unit cost cannot be null");
            }

            if (update.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
                throw new StockValidationException("Unit cost must be positive");
            }

            if (update.getUnitCost().scale() > 2) {
                throw new StockValidationException("Unit cost cannot have more than 2 decimal places");
            }
        });
    }

    private void validateItemsOwnership(List<MenuItem> items) {
        Long restaurantId = userService.getCurrentUser().getRestaurant().getId();
        if (restaurantId == null) {
            throw new IllegalStateException("Current user has no associated restaurant");
        }

        boolean hasUnauthorizedAccess = items.stream()
                .anyMatch(item -> !item.getCategory().getRestaurant().getId().equals(restaurantId));

        if (hasUnauthorizedAccess) {
            throw new AccessDeniedException("Unauthorized access to menu items");
        }

        boolean hasInactiveItems = items.stream()
                .anyMatch(item -> !item.isActive());

        if (hasInactiveItems) {
            throw new StockValidationException("Cannot update inactive menu items");
        }
    }

    private void validateRestaurantAccess(Long restaurantId) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.getRestaurant().getId().equals(restaurantId)) {
            throw new AccessDeniedException("Unauthorized access to restaurant data");
        }
    }
} 