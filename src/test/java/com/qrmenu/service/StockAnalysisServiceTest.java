package com.qrmenu.service;

import com.qrmenu.dto.stock.StockTrendResponse;
import com.qrmenu.model.*;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.StockHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockAnalysisServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private StockAnalysisService stockAnalysisService;

    private Restaurant restaurant;
    private MenuItem menuItem;
    private User user;
    private List<StockHistory> stockHistories;

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
                .stockQuantity(10)
                .build();

        stockHistories = Arrays.asList(
            createStockHistory(5, 10),
            createStockHistory(10, 15),
            createStockHistory(15, 8)
        );

        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void analyzeStockTrends_ShouldCalculateCorrectTrends() {
        when(stockHistoryRepository.findByRestaurantIdAndDateRange(any(), any(), any()))
                .thenReturn(stockHistories);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        List<StockTrendResponse> trends = stockAnalysisService.analyzeStockTrends(1L, 7);

        assertThat(trends).hasSize(1);
        StockTrendResponse trend = trends.get(0);
        assertThat(trend.getAverageQuantity()).isEqualTo(11.0);
        assertThat(trend.getMaxQuantity()).isEqualTo(15);
        assertThat(trend.getMinQuantity()).isEqualTo(8);
        assertThat(trend.getAdjustmentCount()).isEqualTo(3);
    }

    @Test
    void analyzeStockTrends_ShouldReturnEmptyList_WhenNoHistory() {
        when(stockHistoryRepository.findByRestaurantIdAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<StockTrendResponse> trends = stockAnalysisService.analyzeStockTrends(1L, 7);

        assertThat(trends).isEmpty();
    }

    @Test
    void analyzeStockTrends_ShouldThrowException_WhenInvalidDays() {
        assertThrows(IllegalArgumentException.class, () ->
                stockAnalysisService.analyzeStockTrends(1L, 0));
    }

    @Test
    void analyzeStockTrends_ShouldThrowException_WhenUnauthorizedAccess() {
        User otherUser = User.builder()
                .id(2L)
                .restaurant(Restaurant.builder().id(2L).build())
                .build();
        when(userService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(AccessDeniedException.class, () ->
                stockAnalysisService.analyzeStockTrends(1L, 7));
    }

    @Test
    void analyzeStockTrends_ShouldThrowException_WhenNullHistory() {
        when(stockHistoryRepository.findByRestaurantIdAndDateRange(any(), any(), any()))
                .thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                stockAnalysisService.analyzeStockTrends(1L, 7));
    }

    @Test
    void analyzeStockTrends_ShouldValidateMenuItemExists() {
        when(stockHistoryRepository.findByRestaurantIdAndDateRange(any(), any(), any()))
                .thenReturn(stockHistories);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                stockAnalysisService.analyzeStockTrends(1L, 7));
    }

    @Test
    void analyzeStockTrends_ShouldThrowException_WhenPeriodTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
                stockAnalysisService.analyzeStockTrends(1L, 366));
    }

    private StockHistory createStockHistory(int prev, int next) {
        return StockHistory.builder()
                .menuItem(menuItem)
                .previousQuantity(prev)
                .newQuantity(next)
                .adjustmentQuantity(next - prev)
                .adjustedAt(LocalDateTime.now())
                .build();
    }
} 