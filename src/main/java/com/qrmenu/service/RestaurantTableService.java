package com.qrmenu.service;

import com.qrmenu.dto.table.RestaurantTableRequest;
import com.qrmenu.dto.table.RestaurantTableResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.RestaurantTable;
import com.qrmenu.repository.RestaurantRepository;
import com.qrmenu.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing restaurant tables.
 * Provides functionality for creating, updating, retrieving, and deleting tables.
 */
@Service
@RequiredArgsConstructor
public class RestaurantTableService {
    private final RestaurantTableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Creates a new table for the specified restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @param request Table creation request containing table details
     * @return Created table response
     * @throws ResourceNotFoundException if restaurant not found
     * @throws IllegalArgumentException if table number already exists
     */
    @Transactional
    public RestaurantTableResponse createTable(Long restaurantId, RestaurantTableRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (tableRepository.existsByRestaurantIdAndTableNumberAndActiveTrue(
                restaurantId, request.getTableNumber())) {
            throw new IllegalArgumentException("Table number already exists");
        }

        RestaurantTable table = RestaurantTable.builder()
                .restaurant(restaurant)
                .tableNumber(request.getTableNumber())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .active(true)
                .build();

        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    /**
     * Retrieves all active tables for a restaurant.
     *
     * @param restaurantId ID of the restaurant
     * @return List of table responses
     */
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> getRestaurantTables(Long restaurantId) {
        return tableRepository.findByRestaurantIdAndActiveTrue(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing table.
     *
     * @param restaurantId ID of the restaurant
     * @param tableId ID of the table to update
     * @param request Update request containing new table details
     * @return Updated table response
     * @throws ResourceNotFoundException if table not found
     * @throws IllegalArgumentException if table doesn't belong to restaurant or number conflict
     */
    @Transactional
    public RestaurantTableResponse updateTable(Long restaurantId, Long tableId, RestaurantTableRequest request) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Table does not belong to the restaurant");
        }

        if (!table.getTableNumber().equals(request.getTableNumber()) &&
            tableRepository.existsByRestaurantIdAndTableNumberAndActiveTrue(
                restaurantId, request.getTableNumber())) {
            throw new IllegalArgumentException("Table number already exists");
        }

        table.setTableNumber(request.getTableNumber());
        table.setDescription(request.getDescription());
        table.setCapacity(request.getCapacity());

        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    /**
     * Soft deletes a table by marking it as inactive.
     *
     * @param restaurantId ID of the restaurant
     * @param tableId ID of the table to delete
     * @throws ResourceNotFoundException if table not found
     * @throws IllegalArgumentException if table doesn't belong to restaurant
     */
    @Transactional
    public void deleteTable(Long restaurantId, Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Table does not belong to the restaurant");
        }

        table.setActive(false);
        tableRepository.save(table);
    }

    private RestaurantTableResponse mapToResponse(RestaurantTable table) {
        return RestaurantTableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .description(table.getDescription())
                .capacity(table.getCapacity())
                .build();
    }
} 