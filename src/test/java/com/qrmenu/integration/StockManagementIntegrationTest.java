package com.qrmenu.integration;

import com.qrmenu.dto.stock.BatchValuationRequest;
import com.qrmenu.dto.stock.StockValuationResponse;
import com.qrmenu.dto.stock.StockTrendResponse;
import com.qrmenu.model.*;
import com.qrmenu.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockManagementIntegrationTest {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Autowired
    private StockAlertRepository stockAlertRepository;

    @Autowired
    private StockValuationService stockValuationService;

    @Autowired
    private StockAnalysisService stockAnalysisService;

    private Restaurant restaurant;
    private Category category;
    private MenuItem menuItem;
    private User user;

    @BeforeEach
    void setUp() {
        restaurant = restaurantRepository.save(Restaurant.builder()
                .name("Test Restaurant")
                .isActive(true)
                .build());

        category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .restaurant(restaurant)
                .build());

        menuItem = menuItemRepository.save(MenuItem.builder()
                .name("Test Item")
                .category(category)
                .price(BigDecimal.TEN)
                .trackStock(true)
                .stockQuantity(10)
                .unitCost(BigDecimal.valueOf(5.99))
                .active(true)
                .lowStockThreshold(5)
                .build());

        user = userRepository.save(User.builder()
                .email("test@example.com")
                .passwordHash("hash")
                .restaurant(restaurant)
                .role(UserRole.RESTAURANT_ADMIN)
                .active(true)
                .build());
    }

    @Test
    @WithMockUser("test@example.com")
    void updateStockValuation_ShouldCreateHistoryAndAlerts() {
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(menuItem.getId());
        update.setUnitCost(BigDecimal.valueOf(7.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        List<StockValuationResponse> result = stockValuationService.updateValuations(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUnitCost())
                .isEqualByComparingTo(BigDecimal.valueOf(7.99));

        // Verify stock alert was created
        List<StockAlert> alerts = stockAlertRepository
                .findByMenuItemIdAndAcknowledgedAtIsNull(menuItem.getId());
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getAlertType()).isEqualTo(StockAlertType.VALUATION_CHANGE);
    }

    @Test
    @WithMockUser("test@example.com")
    void analyzeStockTrends_ShouldIncludeAllAdjustments() {
        // Create some stock history
        stockHistoryRepository.save(StockHistory.builder()
                .menuItem(menuItem)
                .previousQuantity(10)
                .newQuantity(15)
                .adjustmentQuantity(5)
                .adjustmentType(StockAdjustmentType.MANUAL_ADJUSTMENT)
                .adjustedBy("test@example.com")
                .build());

        List<StockTrendResponse> trends = stockAnalysisService
                .analyzeStockTrends(restaurant.getId(), 7);

        assertThat(trends).hasSize(1);
        assertThat(trends.get(0).getAdjustmentCount()).isEqualTo(1);
        assertThat(trends.get(0).getMaxQuantity()).isEqualTo(15);
    }

    @Test
    @WithMockUser("test@example.com")
    void updateStockValuation_ShouldFailForOtherRestaurant() {
        Restaurant otherRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Other Restaurant")
                .build());

        Category otherCategory = categoryRepository.save(Category.builder()
                .name("Other Category")
                .restaurant(otherRestaurant)
                .build());

        MenuItem otherItem = menuItemRepository.save(MenuItem.builder()
                .name("Other Item")
                .category(otherCategory)
                .price(BigDecimal.TEN)
                .trackStock(true)
                .stockQuantity(10)
                .build());

        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(otherItem.getId());
        update.setUnitCost(BigDecimal.valueOf(7.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        assertThrows(AccessDeniedException.class, () ->
                stockValuationService.updateValuations(request));
    }
} 