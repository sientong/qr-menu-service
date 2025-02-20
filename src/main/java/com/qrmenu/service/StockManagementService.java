package com.qrmenu.service;

import com.qrmenu.dto.stock.BatchStockUpdateRequest;
import com.qrmenu.dto.stock.StockAdjustmentRequest;
import com.qrmenu.dto.stock.StockResponse;
import com.qrmenu.dto.stock.StockHistoryResponse;
import com.qrmenu.dto.stock.StockReportRequest;
import com.qrmenu.dto.stock.StockValuationResponse;
import com.qrmenu.dto.stock.StockReportSummary;
import com.qrmenu.dto.stock.BatchValuationRequest;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.exception.StockManagementException;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.StockHistory;
import com.qrmenu.model.StockAdjustmentType;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.qrmenu.model.User; 

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class StockManagementService {
    private final MenuItemRepository menuItemRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final UserService userService;
    private final StockExportService stockExportService;
    private final StockAlertService stockAlertService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public StockResponse adjustStock(Long menuItemId, StockAdjustmentRequest request) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.isTrackStock()) {
            throw new StockManagementException("Stock tracking is not enabled for this item");
        }

        int previousQuantity = menuItem.getStockQuantity();
        int newQuantity = previousQuantity + request.getQuantity();
        if (newQuantity < 0) {
            throw new StockManagementException("Insufficient stock quantity");
        }

        menuItem.setStockQuantity(newQuantity);
        MenuItem savedItem = menuItemRepository.save(menuItem);

        // Create alerts if needed
        stockAlertService.createLowStockAlert(savedItem);
        if (newQuantity <= 0) {
            stockAlertService.createOutOfStockAlert(savedItem);
        }

        // Track history
        StockHistory history = StockHistory.builder()
                .menuItem(menuItem)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .adjustmentQuantity(request.getQuantity())
                .adjustmentType(StockAdjustmentType.MANUAL_ADJUSTMENT)
                .adjustedBy(userService.getCurrentUser().getEmail())
                .notes(request.getNotes())
                .build();
        stockHistoryRepository.save(history);

        return mapToStockResponse(savedItem);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<StockResponse> batchUpdateStock(BatchStockUpdateRequest request) {
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            throw new IllegalArgumentException("Stock updates cannot be empty");
        }

        List<Long> itemIds = request.getUpdates().stream()
                .map(BatchStockUpdateRequest.StockUpdate::getMenuItemId)
                .collect(Collectors.toList());

        List<MenuItem> items = menuItemRepository.findAllById(itemIds);
        if (items.size() != itemIds.size()) {
            Set<Long> foundIds = items.stream()
                    .map(MenuItem::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = itemIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Menu items not found: " + missingIds);
        }

        // Validate all items have stock tracking enabled
        List<MenuItem> nonTrackingItems = items.stream()
                .filter(item -> !item.isTrackStock())
                .collect(Collectors.toList());
        if (!nonTrackingItems.isEmpty()) {
            throw new StockManagementException("Stock tracking not enabled for items: " + 
                nonTrackingItems.stream().map(MenuItem::getName).collect(Collectors.joining(", ")));
        }

        Map<Long, Integer> quantityUpdates = request.getUpdates().stream()
                .collect(Collectors.toMap(
                    BatchStockUpdateRequest.StockUpdate::getMenuItemId,
                    BatchStockUpdateRequest.StockUpdate::getQuantity
                ));

        // Track history for each item
        User currentUser = userService.getCurrentUser();
        items.forEach(item -> {
            int previousQuantity = item.getStockQuantity();
            int newQuantity = quantityUpdates.get(item.getId());
            
            if (newQuantity < 0) {
                throw new StockManagementException("Invalid negative stock quantity for item: " + item.getName());
            }
            
            item.setStockQuantity(newQuantity);
            
            StockHistory history = StockHistory.builder()
                    .menuItem(item)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .adjustmentQuantity(newQuantity - previousQuantity)
                    .adjustmentType(StockAdjustmentType.BATCH_UPDATE)
                    .adjustedBy(currentUser.getEmail())
                    .build();
            stockHistoryRepository.save(history);
            
            // Check for alerts
            stockAlertService.createLowStockAlert(item);
            if (newQuantity <= 0) {
                stockAlertService.createOutOfStockAlert(item);
            }
        });

        List<MenuItem> savedItems = menuItemRepository.saveAll(items);
        return savedItems.stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockHistoryResponse> getStockHistory(Long menuItemId) {
        return stockHistoryRepository.findByMenuItemIdOrderByAdjustedAtDesc(menuItemId)
                .stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockHistoryResponse> generateStockReport(StockReportRequest request) {
        User currentUser = userService.getCurrentUser();
        List<StockHistory> history;
        
        if (request.getMenuItemId() != null) {
            history = stockHistoryRepository.findByMenuItemIdAndAdjustedAtBetweenOrderByAdjustedAtDesc(
                    request.getMenuItemId(), request.getStartDate(), request.getEndDate());
        } else {
            history = stockHistoryRepository.findByRestaurantIdAndDateRange(
                    currentUser.getRestaurant().getId(), request.getStartDate(), request.getEndDate());
        }

        return history.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockReportSummary generateReportSummary(StockReportRequest request) {
        User currentUser = userService.getCurrentUser();
        List<MenuItem> items = menuItemRepository.findByRestaurantId(currentUser.getRestaurant().getId());
        List<StockHistory> history = stockHistoryRepository.findByRestaurantIdAndDateRange(
                currentUser.getRestaurant().getId(), request.getStartDate(), request.getEndDate());

        BigDecimal totalValue = BigDecimal.ZERO;
        int lowStockCount = 0;
        int outOfStockCount = 0;
        int totalQuantity = 0;

        for (MenuItem item : items) {
            if (item.isTrackStock()) {
                totalQuantity += item.getStockQuantity();
                totalValue = totalValue.add(item.getUnitCost().multiply(BigDecimal.valueOf(item.getStockQuantity())));
                
                if (item.getStockQuantity() <= item.getLowStockThreshold()) {
                    lowStockCount++;
                }
                if (item.getStockQuantity() == 0) {
                    outOfStockCount++;
                }
            }
        }

        // Find most adjusted item
        Map<Long, Long> adjustmentCounts = history.stream()
                .collect(Collectors.groupingBy(h -> h.getMenuItem().getId(), Collectors.counting()));
        
        String mostAdjustedItem = adjustmentCounts.isEmpty() ? "None" :
                items.stream()
                        .filter(i -> i.getId().equals(Collections.max(adjustmentCounts.entrySet(),
                                Map.Entry.comparingByValue()).getKey()))
                        .findFirst()
                        .map(MenuItem::getName)
                        .orElse("None");

        // Find most valuable item
        String mostValuableItem = items.stream()
                .filter(MenuItem::isTrackStock)
                .max(Comparator.comparing(i -> i.getUnitCost().multiply(BigDecimal.valueOf(i.getStockQuantity()))))
                .map(MenuItem::getName)
                .orElse("None");

        return StockReportSummary.builder()
                .totalItems(items.size())
                .totalStockValue(totalValue)
                .lowStockItemsCount(lowStockCount)
                .outOfStockItemsCount(outOfStockCount)
                .averageStockLevel(items.isEmpty() ? 0 : (double) totalQuantity / items.size())
                .mostAdjustedItem(mostAdjustedItem)
                .mostValuableItem(mostValuableItem)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportStockReport(StockReportRequest request) {
        List<StockHistoryResponse> history = generateStockReport(request);
        StockReportSummary summary = generateReportSummary(request);
        return stockExportService.exportToExcel(history, summary);
    }

    @Transactional
    public List<StockValuationResponse> batchUpdateValuation(BatchValuationRequest request) {
        List<Long> itemIds = request.getUpdates().stream()
                .map(BatchValuationRequest.ValuationUpdate::getMenuItemId)
                .collect(Collectors.toList());

        List<MenuItem> items = menuItemRepository.findAllById(itemIds);
        if (items.size() != itemIds.size()) {
            throw new ResourceNotFoundException("Some menu items were not found");
        }

        Map<Long, BigDecimal> costUpdates = request.getUpdates().stream()
                .collect(Collectors.toMap(
                    BatchValuationRequest.ValuationUpdate::getMenuItemId,
                    BatchValuationRequest.ValuationUpdate::getUnitCost
                ));

        items.forEach(item -> {
            BigDecimal oldCost = item.getUnitCost();
            BigDecimal newCost = costUpdates.get(item.getId());
            
            if (oldCost != null && oldCost.compareTo(newCost) != 0) {
                BigDecimal changePercent = newCost.subtract(oldCost)
                        .divide(oldCost, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                
                stockAlertService.createValuationChangeAlert(item,
                        String.format("Unit cost for %s changed by %.2f%% (from %s to %s)",
                                item.getName(), changePercent, oldCost, newCost));
            }
            
            item.setUnitCost(newCost);
            item.setLastValuationDate(LocalDateTime.now());
        });

        return menuItemRepository.saveAll(items).stream()
                .map(this::mapToValuationResponse)
                .collect(Collectors.toList());
    }

    private StockResponse mapToStockResponse(MenuItem menuItem) {
        return StockResponse.builder()
                .menuItemId(menuItem.getId())
                .stockQuantity(menuItem.getStockQuantity())
                .lowStockThreshold(menuItem.getLowStockThreshold())
                .trackStock(menuItem.isTrackStock())
                .active(menuItem.isActive())
                .lowStock(menuItem.getStockQuantity() <= menuItem.getLowStockThreshold())
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

    private StockValuationResponse mapToValuationResponse(MenuItem item) {
        return StockValuationResponse.builder()
                .menuItemId(item.getId())
                .menuItemName(item.getName())
                .stockQuantity(item.getStockQuantity())
                .unitCost(item.getUnitCost())
                .totalValue(item.getUnitCost().multiply(BigDecimal.valueOf(item.getStockQuantity())))
                .lastValuationDate(item.getLastValuationDate())
                .build();
    }
} 