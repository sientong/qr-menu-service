package com.qrmenu.repository;

import com.qrmenu.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StockHistoryRepositoryTest {

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Restaurant restaurant;
    private MenuItem menuItem;
    private StockHistory stockHistory;

    @BeforeEach
    void setUp() {
        restaurant = restaurantRepository.save(Restaurant.builder()
                .name("Test Restaurant")
                .isActive(true)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .restaurant(restaurant)
                .build());

        menuItem = menuItemRepository.save(MenuItem.builder()
                .name("Test Item")
                .category(category)
                .trackStock(true)
                .stockQuantity(10)
                .build());

        stockHistory = stockHistoryRepository.save(StockHistory.builder()
                .menuItem(menuItem)
                .previousQuantity(5)
                .newQuantity(10)
                .adjustmentQuantity(5)
                .adjustmentType(StockAdjustmentType.MANUAL_ADJUSTMENT)
                .adjustedBy("test@example.com")
                .adjustedAt(LocalDateTime.now())
                .build());
    }

    @Test
    void findByRestaurantIdAndDateRange_ShouldReturnCorrectHistory() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        List<StockHistory> history = stockHistoryRepository
                .findByRestaurantIdAndDateRange(restaurant.getId(), startDate, endDate);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getMenuItem().getId()).isEqualTo(menuItem.getId());
    }

    @Test
    void findByMenuItemIdOrderByAdjustedAtDesc_ShouldReturnOrderedHistory() {
        List<StockHistory> history = stockHistoryRepository
                .findByMenuItemIdOrderByAdjustedAtDesc(menuItem.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getId()).isEqualTo(stockHistory.getId());
    }

    @Test
    void findByRestaurantIdAndDateRange_ShouldReturnEmptyList_WhenOutsideDateRange() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        List<StockHistory> history = stockHistoryRepository
                .findByRestaurantIdAndDateRange(restaurant.getId(), startDate, endDate);

        assertThat(history).isEmpty();
    }

    @Test
    void findByRestaurantIdAndDateRange_ShouldReturnEmptyList_WhenWrongRestaurant() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        List<StockHistory> history = stockHistoryRepository
                .findByRestaurantIdAndDateRange(999L, startDate, endDate);

        assertThat(history).isEmpty();
    }

    @Test
    void countRecentAdjustments_ShouldReturnCorrectCount() {
        // Add another history entry
        stockHistoryRepository.save(StockHistory.builder()
                .menuItem(menuItem)
                .previousQuantity(10)
                .newQuantity(15)
                .adjustmentQuantity(5)
                .adjustmentType(StockAdjustmentType.MANUAL_ADJUSTMENT)
                .adjustedBy("test@example.com")
                .adjustedAt(LocalDateTime.now())
                .build());

        long count = stockHistoryRepository.countRecentAdjustments(
                menuItem.getId(), 
                LocalDateTime.now().minusDays(1)
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countRecentAdjustments_ShouldReturnZero_WhenOutsideDateRange() {
        long count = stockHistoryRepository.countRecentAdjustments(
                menuItem.getId(),
                LocalDateTime.now().plusDays(1)
        );

        assertThat(count).isZero();
    }
} 