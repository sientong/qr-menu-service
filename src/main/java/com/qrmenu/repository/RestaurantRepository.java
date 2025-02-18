package com.qrmenu.repository;

import com.qrmenu.model.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
    Optional<Restaurant> findById(Long id);
    List<Restaurant> findAll();
    List<Restaurant> findAllActive();
    void delete(Long id);
    boolean existsByName(String name);
} 