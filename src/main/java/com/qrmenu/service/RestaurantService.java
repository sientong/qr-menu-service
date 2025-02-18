package com.qrmenu.service;

import com.qrmenu.model.Restaurant;
import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    Restaurant createRestaurant(Restaurant restaurant);
    Restaurant updateRestaurant(Restaurant restaurant);
    Optional<Restaurant> findById(Long id);
    List<Restaurant> findAllRestaurants();
    List<Restaurant> findAllActiveRestaurants();
    void deleteRestaurant(Long id);
    boolean existsByName(String name);
} 