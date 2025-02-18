package com.qrmenu.repository;

import com.qrmenu.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRestaurantIdAndActiveTrue(Long restaurantId);
    
    Optional<RestaurantTable> findByRestaurantIdAndTableNumberAndActiveTrue(
        Long restaurantId, 
        String tableNumber
    );
    
    boolean existsByRestaurantIdAndTableNumberAndActiveTrue(
        Long restaurantId, 
        String tableNumber
    );
} 