package com.qrmenu.controller;

import com.qrmenu.dto.RestaurantCreateRequest;
import com.qrmenu.dto.RestaurantResponse;
import com.qrmenu.dto.RestaurantUpdateRequest;
import com.qrmenu.model.Restaurant;
import com.qrmenu.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

import com.qrmenu.exception.DuplicateResourceException;
import com.qrmenu.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Management", description = "APIs for managing restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @Operation(summary = "Create a new restaurant")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Restaurant with same name already exists")
    })
    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantCreateRequest request) {
        if (restaurantService.existsByName(request.getName())) {
            throw new DuplicateResourceException("Restaurant", "name", request.getName());
        }

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .address(request.getAddress())
                .logoUrl(request.getLogoUrl())
                .active(request.isActive())
                .build();

        Restaurant created = restaurantService.createRestaurant(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestaurantResponse.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantUpdateRequest request) {
        
        if (!id.equals(request.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return restaurantService.findById(id)
                .map(restaurant -> {
                    if (request.getName() != null) {
                        restaurant.setName(request.getName());
                    }
                    if (request.getDescription() != null) {
                        restaurant.setDescription(request.getDescription());
                    }
                    if (request.getWebsite() != null) {
                        restaurant.setWebsite(request.getWebsite());
                    }
                    if (request.getPhone() != null) {
                        restaurant.setPhone(request.getPhone());
                    }
                    if (request.getAddress() != null) {
                        restaurant.setAddress(request.getAddress());
                    }
                    if (request.getLogoUrl() != null) {
                        restaurant.setLogoUrl(request.getLogoUrl());
                    }
                    if (request.getActive() != null) {
                        restaurant.setActive(request.getActive());
                    }
                    restaurant.setUpdatedAt(LocalDateTime.now());

                    Restaurant updated = restaurantService.updateRestaurant(restaurant);
                    return ResponseEntity.ok(RestaurantResponse.from(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return ResponseEntity.ok(RestaurantResponse.from(restaurant));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<Restaurant> restaurants = activeOnly ?
                restaurantService.findAllActiveRestaurants() :
                restaurantService.findAllRestaurants();

        List<RestaurantResponse> response = restaurants.stream()
                .map(RestaurantResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        if (restaurantService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
} 