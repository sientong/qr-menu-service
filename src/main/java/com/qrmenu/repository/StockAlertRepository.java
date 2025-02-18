package com.qrmenu.repository;

import com.qrmenu.model.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    List<StockAlert> findByMenuItemIdAndAcknowledgedAtIsNull(Long menuItemId);
    List<StockAlert> findByMenuItemCategoryRestaurantIdAndAcknowledgedAtIsNull(Long restaurantId);
    long countByAcknowledgedAtIsNull();
    void deleteByCreatedAtBeforeAndAcknowledgedAtIsNotNull(LocalDateTime cutoff);
} 