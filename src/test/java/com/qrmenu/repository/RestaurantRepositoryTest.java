package com.qrmenu.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.qrmenu.model.Restaurant;

@SpringBootTest
@Transactional
@Sql("/db/cleanup.sql")
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void shouldSaveNewRestaurant() {
        // Given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .description("Test Description")
                .phone("+1234567890")
                .address("Test Address")
                .active(true)
                .build();

        // When
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Then
        assertThat(savedRestaurant.getId()).isNotNull();
        assertThat(savedRestaurant.getName()).isEqualTo("Test Restaurant");
        assertThat(savedRestaurant.isActive()).isTrue();
    }

    @Test
    void shouldFindAllActiveRestaurants() {
        // Given
        Restaurant active1 = Restaurant.builder()
                .name("Active Restaurant 1")
                .active(true)
                .build();
        Restaurant active2 = Restaurant.builder()
                .name("Active Restaurant 2")
                .active(true)
                .build();
        Restaurant inactive = Restaurant.builder()
                .name("Inactive Restaurant")
                .active(false)
                .build();

        restaurantRepository.save(active1);
        restaurantRepository.save(active2);
        restaurantRepository.save(inactive);

        // When
        List<Restaurant> activeRestaurants = restaurantRepository.findAllActive();

        // Then
        assertThat(activeRestaurants).hasSize(2);
        assertThat(activeRestaurants).extracting("active").containsOnly(true);
    }

    @Test
    void shouldUpdateExistingRestaurant() {
        // Given
        Restaurant restaurant = Restaurant.builder()
                .name("Original Name")
                .description("Original Description")
                .active(true)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // When
        savedRestaurant.setName("Updated Name");
        savedRestaurant.setDescription("Updated Description");
        savedRestaurant.setUpdatedAt(LocalDateTime.now());
        Restaurant updatedRestaurant = restaurantRepository.save(savedRestaurant);

        // Then
        assertThat(updatedRestaurant.getName()).isEqualTo("Updated Name");
        assertThat(updatedRestaurant.getDescription()).isEqualTo("Updated Description");
    }
}