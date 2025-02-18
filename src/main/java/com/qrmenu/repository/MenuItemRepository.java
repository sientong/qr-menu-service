package com.qrmenu.repository;

import com.qrmenu.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);
    List<MenuItem> findByCategoryIdAndActiveOrderByDisplayOrderAsc(Long categoryId, boolean active);
    
    // Stock management queries
    long countByTrackStockTrueAndStockQuantityLessThanEqual(int quantity);
    List<MenuItem> findByTrackStockTrueAndStockQuantityLessThanEqual(int quantity);

    @Query("SELECT m FROM MenuItem m WHERE m.category.restaurant.id = :restaurantId")
    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByMenuId(Long menuId);
} 