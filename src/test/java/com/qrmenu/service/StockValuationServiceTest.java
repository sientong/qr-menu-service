package com.qrmenu.service;

import com.qrmenu.dto.stock.BatchValuationRequest;
import com.qrmenu.dto.stock.StockValuationResponse;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.User;
import com.qrmenu.model.Category;
import com.qrmenu.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockValuationServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private StockAlertService stockAlertService;

    @Mock
    private UserService userService;

    @InjectMocks
    private StockValuationService stockValuationService;

    private MenuItem menuItem1;
    private MenuItem menuItem2;
    private Restaurant restaurant;
    private User user;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .build();

        Category category = Category.builder()
                .id(1L)
                .restaurant(restaurant)
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .restaurant(restaurant)
                .build();

        menuItem1 = MenuItem.builder()
                .id(1L)
                .name("Item 1")
                .trackStock(true)
                .stockQuantity(10)
                .unitCost(BigDecimal.valueOf(5.99))
                .category(category)
                .build();

        menuItem2 = MenuItem.builder()
                .id(2L)
                .name("Item 2")
                .trackStock(true)
                .stockQuantity(5)
                .unitCost(BigDecimal.valueOf(3.99))
                .category(category)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getValuations_ShouldReturnValuationsForRestaurant() {
        when(menuItemRepository.findByRestaurantId(1L))
                .thenReturn(Arrays.asList(menuItem1, menuItem2));

        List<StockValuationResponse> result = stockValuationService.getValuations(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTotalValue())
                .isEqualByComparingTo(BigDecimal.valueOf(59.90));
        assertThat(result.get(1).getTotalValue())
                .isEqualByComparingTo(BigDecimal.valueOf(19.95));
    }

    @Test
    void updateValuations_ShouldUpdateAndCreateAlerts() {
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(1L);
        update.setUnitCost(BigDecimal.valueOf(7.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        when(menuItemRepository.findAllById(any())).thenReturn(Arrays.asList(menuItem1));
        when(menuItemRepository.saveAll(any())).thenReturn(Arrays.asList(menuItem1));

        List<StockValuationResponse> result = stockValuationService.updateValuations(request);

        assertThat(result).hasSize(1);
        verify(stockAlertService).createValuationChangeAlert(eq(menuItem1), any());
    }

    @Test
    void updateValuations_ShouldThrowException_WhenRequestIsNull() {
        assertThrows(StockValidationException.class, () -> 
            stockValuationService.updateValuations(null));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenUpdatesIsEmpty() {
        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Collections.emptyList());
        
        assertThrows(StockValidationException.class, () -> 
            stockValuationService.updateValuations(request));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenUnitCostIsNegative() {
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(1L);
        update.setUnitCost(BigDecimal.valueOf(-1.0));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        assertThrows(StockValidationException.class, () -> 
            stockValuationService.updateValuations(request));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenItemNotFound() {
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(999L);
        update.setUnitCost(BigDecimal.valueOf(5.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        when(menuItemRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> 
            stockValuationService.updateValuations(request));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenUnauthorizedAccess() {
        Restaurant otherRestaurant = Restaurant.builder().id(2L).build();
        Category otherCategory = Category.builder()
                .id(2L)
                .restaurant(otherRestaurant)
                .build();

        MenuItem unauthorizedItem = MenuItem.builder()
                .id(3L)
                .category(otherCategory)
                .build();

        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(3L);
        update.setUnitCost(BigDecimal.valueOf(5.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        when(menuItemRepository.findAllById(any())).thenReturn(Arrays.asList(unauthorizedItem));

        assertThrows(AccessDeniedException.class, () -> 
            stockValuationService.updateValuations(request));
    }

    @Test
    void getValuations_ShouldThrowException_WhenRestaurantIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            stockValuationService.getValuations(null));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenUnitCostHasInvalidScale() {
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(1L);
        update.setUnitCost(BigDecimal.valueOf(5.999)); // 3 decimal places

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        assertThrows(StockValidationException.class, () ->
            stockValuationService.updateValuations(request));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenTrackingNotEnabled() {
        menuItem1.setTrackStock(false);
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(1L);
        update.setUnitCost(BigDecimal.valueOf(5.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        when(menuItemRepository.findAllById(any())).thenReturn(Arrays.asList(menuItem1));

        assertThrows(StockValidationException.class, () ->
            stockValuationService.updateValuations(request));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenItemIsInactive() {
        menuItem1.setActive(false);
        BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
        update.setMenuItemId(1L);
        update.setUnitCost(BigDecimal.valueOf(5.99));

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(update));

        when(menuItemRepository.findAllById(any())).thenReturn(Arrays.asList(menuItem1));

        assertThrows(StockValidationException.class, () ->
            stockValuationService.updateValuations(request));
    }

    @Test
    void updateValuations_ShouldThrowException_WhenTooManyUpdates() {
        List<BatchValuationRequest.ValuationUpdate> updates = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            BatchValuationRequest.ValuationUpdate update = new BatchValuationRequest.ValuationUpdate();
            update.setMenuItemId((long) i);
            update.setUnitCost(BigDecimal.ONE);
            updates.add(update);
        }

        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(updates);

        assertThrows(StockValidationException.class, () ->
            stockValuationService.updateValuations(request));
    }
} 