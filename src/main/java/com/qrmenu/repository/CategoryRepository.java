package com.qrmenu.repository;

import com.qrmenu.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantIdOrderByDisplayOrderAsc(Long restaurantId);
    List<Category> findByRestaurantIdAndActiveOrderByDisplayOrderAsc(Long restaurantId, boolean active);
    
    // Added methods
    boolean existsByRestaurantIdAndNameAndActiveTrue(Long restaurantId, String name);
    Optional<Category> findByIdAndActiveTrue(Long id);
    List<Category> findByRestaurantIdAndActiveTrueAndIdIn(Long restaurantId, List<Long> ids);
}