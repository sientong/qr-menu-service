package com.qrmenu.repository;

import com.qrmenu.model.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findByMenuItemIdOrderByAdjustedAtDesc(Long menuItemId);
    
    List<StockHistory> findByMenuItemIdAndAdjustedAtBetweenOrderByAdjustedAtDesc(
            Long menuItemId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT h FROM StockHistory h WHERE h.menuItem.category.restaurant.id = :restaurantId " +
           "AND h.adjustedAt BETWEEN :startDate AND :endDate")
    List<StockHistory> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(h) FROM StockHistory h WHERE h.menuItem.id = :menuItemId " +
           "AND h.adjustedAt >= :since")
    long countRecentAdjustments(@Param("menuItemId") Long menuItemId, 
                               @Param("since") LocalDateTime since);
} 