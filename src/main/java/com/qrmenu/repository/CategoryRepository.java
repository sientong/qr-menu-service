package com.qrmenu.repository;

import com.qrmenu.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantIdOrderByDisplayOrderAsc(Long restaurantId);
    List<Category> findByRestaurantIdAndActiveOrderByDisplayOrderAsc(Long restaurantId, boolean active);
} 