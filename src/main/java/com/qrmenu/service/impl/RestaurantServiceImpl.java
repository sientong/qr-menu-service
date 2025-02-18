package com.qrmenu.service.impl;

import com.qrmenu.model.Restaurant;
import com.qrmenu.repository.RestaurantRepository;
import com.qrmenu.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public Restaurant updateRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findAllActiveRestaurants() {
        return restaurantRepository.findAllActive();
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }
} 