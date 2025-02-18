package com.qrmenu.repository;

import com.qrmenu.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    // Basic CRUD operations are provided by JpaRepository
    List<Menu> findByRestaurantId(Long restaurantId);
    List<Menu> findByRestaurantIdAndActiveTrue(Long restaurantId);
} 