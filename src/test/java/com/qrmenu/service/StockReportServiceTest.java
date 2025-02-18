package com.qrmenu.service;

import com.qrmenu.dto.stock.StockHistoryResponse;
import com.qrmenu.dto.stock.StockReportRequest;
import com.qrmenu.dto.stock.StockReportSummary;
import com.qrmenu.model.*;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.StockHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReportServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private StockValuationService valuationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private StockReportService stockReportService;

    private MenuItem menuItem;
    private StockHistory stockHistory;
    private User user;
    private Restaurant restaurant;
    private StockReportRequest request;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder().id(1L).build();
        user = User.builder()
                .id(1L)
                .restaurant(restaurant)
                .build();

        menuItem = MenuItem.builder()
                .id(1L)
                .name("Test Item")
                .trackStock(true)
                .stockQuantity(10)
                .lowStockThreshold(5)
                .unitCost(BigDecimal.valueOf(5.99))
                .build();

        stockHistory = StockHistory.builder()
                .id(1L)
                .menuItem(menuItem)
                .previousQuantity(5)
                .newQuantity(10)
                .adjustmentQuantity(5)
                .adjustmentType(StockAdjustmentType.MANUAL_ADJUSTMENT)
                .adjustedBy("test@example.com")
                .adjustedAt(LocalDateTime.now())
                .build();

        request = new StockReportRequest();
        request.setStartDate(LocalDateTime.now().minusDays(7));
        request.setEndDate(LocalDateTime.now());
    }

    @Test
    void generateReport_ShouldReturnHistoryForDateRange() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(stockHistoryRepository.findByRestaurantIdAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(stockHistory));

        List<StockHistoryResponse> result = stockReportService.generateReport(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMenuItemId()).isEqualTo(menuItem.getId());
        assertThat(result.get(0).getAdjustmentQuantity()).isEqualTo(5);
    }

    @Test
    void generateSummary_ShouldCalculateCorrectMetrics() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(menuItemRepository.findByRestaurantId(restaurant.getId()))
                .thenReturn(Arrays.asList(menuItem));
        when(stockHistoryRepository.findByRestaurantIdAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(stockHistory));

        StockReportSummary result = stockReportService.generateSummary(request);

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getTotalStockValue())
                .isEqualByComparingTo(BigDecimal.valueOf(59.90));
        assertThat(result.getLowStockItemsCount()).isEqualTo(0);
        assertThat(result.getOutOfStockItemsCount()).isEqualTo(0);
    }
} 