package com.qrmenu.repository;

import com.qrmenu.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);
    List<MenuItem> findByCategoryIdAndActiveOrderByDisplayOrderAsc(Long categoryId, boolean active);
    
    // Stock management queries
    long countByTrackStockTrueAndStockQuantityLessThanEqual(int quantity);
    List<MenuItem> findByTrackStockTrueAndStockQuantityLessThanEqual(int quantity);

    @Query("SELECT m FROM MenuItem m WHERE m.category.restaurant.id = :restaurantId")
    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByMenuId(Long menuId);
    
    // Find active menu item by ID
    Optional<MenuItem> findByIdAndActiveTrue(Long id);
    
    // Check if a menu item with the given name exists in a category
    boolean existsByCategoryIdAndNameAndActiveTrue(Long categoryId, String name);
    
    // Check if a menu item with the given name exists in a category, excluding a specific item
    @Query("SELECT COUNT(m) > 0 FROM MenuItem m " +
           "WHERE m.category.id = :categoryId " +
           "AND m.name = :name " +
           "AND m.active = true " +
           "AND m.id != :excludeItemId")
    boolean existsByCategoryIdAndNameAndActiveTrueAndIdNot(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("excludeItemId") Long excludeItemId);
}