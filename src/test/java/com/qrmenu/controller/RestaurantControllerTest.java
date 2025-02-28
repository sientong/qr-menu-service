package com.qrmenu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.RestaurantCreateRequest;
import com.qrmenu.dto.RestaurantUpdateRequest;
import com.qrmenu.model.Restaurant;
import com.qrmenu.service.RestaurantService;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantService restaurantService;

    @Test
    void shouldCreateRestaurant() throws Exception {
        // Given
        RestaurantCreateRequest request = new RestaurantCreateRequest();
        request.setName("Test Restaurant");
        request.setPhone("+1234567890");

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name(request.getName())
                .phone(request.getPhone())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(restaurantService.existsByName(request.getName())).thenReturn(false);
        when(restaurantService.createRestaurant(any())).thenReturn(restaurant);

        // When & Then
        mockMvc.perform(post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(restaurant.getId()))
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    void shouldReturnConflictWhenCreatingRestaurantWithDuplicateName() throws Exception {
        // Given
        RestaurantCreateRequest request = new RestaurantCreateRequest();
        request.setName("Existing Restaurant");
        request.setPhone("+1234567890");

        when(restaurantService.existsByName(request.getName()))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Restaurant already exists with name : 'Existing Restaurant'"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingRestaurantWithInvalidData() throws Exception {
        // Given
        RestaurantCreateRequest request = new RestaurantCreateRequest();
        request.setName(""); // Invalid: empty name
        request.setWebsite("invalid-url"); // Invalid URL format
        request.setPhone("123"); // Invalid phone format

        // When & Then
        mockMvc.perform(post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void shouldGetAllRestaurants() throws Exception {
        // Given
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .active(true)
                .build();

        when(restaurantService.findAllRestaurants()).thenReturn(List.of(restaurant));

        // When & Then
        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(restaurant.getId()))
                .andExpect(jsonPath("$[0].name").value(restaurant.getName()));
    }

    @Test
    void shouldUpdateRestaurant() throws Exception {
        // Given
        Long restaurantId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest();
        request.setId(restaurantId);
        request.setName("Updated Name");

        Restaurant existingRestaurant = Restaurant.builder()
                .id(restaurantId)
                .name("Original Name")
                .active(true)
                .build();

        Restaurant updatedRestaurant = Restaurant.builder()
                .id(restaurantId)
                .name(request.getName())
                .active(true)
                .build();

        when(restaurantService.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
        when(restaurantService.updateRestaurant(any())).thenReturn(updatedRestaurant);

        // When & Then
        mockMvc.perform(put("/api/v1/restaurants/" + restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(restaurantId))
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentRestaurant() throws Exception {
        // Given
        Long nonExistentId = 999L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest();
        request.setId(nonExistentId);
        request.setName("Updated Name");

        when(restaurantService.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/v1/restaurants/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenPathIdDoesNotMatchBodyId() throws Exception {
        // Given
        Long pathId = 1L;
        RestaurantUpdateRequest request = new RestaurantUpdateRequest();
        request.setId(2L); // Different from path ID

        // When & Then
        mockMvc.perform(put("/api/v1/restaurants/" + pathId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleDeleteNonExistentRestaurant() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(restaurantService.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/v1/restaurants/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnActiveRestaurantsOnly() throws Exception {
        // Given
        Restaurant activeRestaurant = Restaurant.builder()
                .id(1L)
                .name("Active Restaurant")
                .active(true)
                .build();

        when(restaurantService.findAllActiveRestaurants())
                .thenReturn(List.of(activeRestaurant));

        // When & Then
        mockMvc.perform(get("/api/v1/restaurants")
                .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Active Restaurant"));
    }
}