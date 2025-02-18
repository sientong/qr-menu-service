package com.qrmenu.service;

import com.qrmenu.dto.stock.BatchValuationRequest;
import com.qrmenu.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class StockValuationServiceSecurityTest {

    @Autowired
    private StockValuationService stockValuationService;

    @Test
    @WithMockUser(roles = "WAITER")
    void getValuations_ShouldThrowException_WhenUserNotAuthorized() {
        assertThrows(AccessDeniedException.class, () ->
            stockValuationService.getValuations(1L));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void updateValuations_ShouldThrowException_WhenUserNotAdmin() {
        BatchValuationRequest request = new BatchValuationRequest();
        request.setUpdates(Arrays.asList(new BatchValuationRequest.ValuationUpdate()));

        assertThrows(AccessDeniedException.class, () ->
            stockValuationService.updateValuations(request));
    }
} 