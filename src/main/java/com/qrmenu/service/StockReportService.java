package com.qrmenu.service;

import com.qrmenu.dto.stock.StockHistoryResponse;
import com.qrmenu.dto.stock.StockReportRequest;
import com.qrmenu.dto.stock.StockReportSummary;
import com.qrmenu.model.MenuItem;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.qrmenu.model.StockHistory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReportService {
    private final MenuItemRepository menuItemRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<StockHistoryResponse> generateReport(StockReportRequest request) {
        Long restaurantId = userService.getCurrentUser().getRestaurant().getId();
        return stockHistoryRepository.findByRestaurantIdAndDateRange(
                restaurantId, request.getStartDate(), request.getEndDate())
                .stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockReportSummary generateSummary(StockReportRequest request) {
        Long restaurantId = userService.getCurrentUser().getRestaurant().getId();
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);
        
        int totalItems = items.size();
        int lowStockCount = 0;
        int outOfStockCount = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (MenuItem item : items) {
            if (item.isTrackStock()) {
                if (item.getStockQuantity() <= 0) {
                    outOfStockCount++;
                } else if (item.getStockQuantity() <= item.getLowStockThreshold()) {
                    lowStockCount++;
                }

                totalQuantity += item.getStockQuantity();
                if (item.getUnitCost() != null) {
                    totalValue = totalValue.add(
                            item.getUnitCost().multiply(BigDecimal.valueOf(item.getStockQuantity()))
                    );
                }
            }
        }

        Map<Long, Long> adjustmentCounts = stockHistoryRepository
                .findByRestaurantIdAndDateRange(restaurantId, request.getStartDate(), request.getEndDate())
                .stream()
                .collect(Collectors.groupingBy(h -> h.getMenuItem().getId(), Collectors.counting()));

        String mostAdjustedItem = !adjustmentCounts.isEmpty() 
                ? items.stream()
                        .filter(i -> i.getId().equals(
                                adjustmentCounts.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse(null)))
                        .findFirst()
                        .map(MenuItem::getName)
                        .orElse("None")
                : "None";

        return StockReportSummary.builder()
                .totalItems(totalItems)
                .totalStockValue(totalValue)
                .lowStockItemsCount(lowStockCount)
                .outOfStockItemsCount(outOfStockCount)
                .averageStockLevel(totalItems > 0 ? (double) totalQuantity / totalItems : 0)
                .mostAdjustedItem(mostAdjustedItem)
                .build();
    }

    private StockHistoryResponse mapToHistoryResponse(StockHistory history) {
        return StockHistoryResponse.builder()
                .id(history.getId())
                .menuItemId(history.getMenuItem().getId())
                .menuItemName(history.getMenuItem().getName())
                .previousQuantity(history.getPreviousQuantity())
                .newQuantity(history.getNewQuantity())
                .adjustmentQuantity(history.getAdjustmentQuantity())
                .adjustmentType(history.getAdjustmentType())
                .adjustedBy(history.getAdjustedBy())
                .adjustedAt(history.getAdjustedAt())
                .notes(history.getNotes())
                .build();
    }
} 