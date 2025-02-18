package com.qrmenu.controller;

import com.qrmenu.dto.table.RestaurantTableRequest;
import com.qrmenu.dto.table.RestaurantTableResponse;
import com.qrmenu.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/tables")
@RequiredArgsConstructor
@Tag(name = "Restaurant Tables", description = "APIs for managing restaurant tables")
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    @Operation(
        summary = "Create a new table",
        description = "Creates a new table for the specified restaurant. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "201", description = "Table created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public RestaurantTableResponse createTable(
            @Parameter(description = "ID of the restaurant") 
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantTableRequest request) {
        return tableService.createTable(restaurantId, request);
    }

    @Operation(
        summary = "Get restaurant tables",
        description = "Retrieves all active tables for the specified restaurant."
    )
    @ApiResponse(responseCode = "200", description = "Tables retrieved successfully")
    @GetMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    public List<RestaurantTableResponse> getRestaurantTables(
            @Parameter(description = "ID of the restaurant")
            @PathVariable Long restaurantId) {
        return tableService.getRestaurantTables(restaurantId);
    }

    @Operation(
        summary = "Update an existing table",
        description = "Updates the details of an existing table. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Table updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or table number conflict")
    @ApiResponse(responseCode = "404", description = "Table or restaurant not found")
    @PutMapping("/{tableId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public RestaurantTableResponse updateTable(
            @Parameter(description = "ID of the restaurant") 
            @PathVariable Long restaurantId,
            @Parameter(description = "ID of the table to update") 
            @PathVariable Long tableId,
            @Valid @RequestBody RestaurantTableRequest request) {
        return tableService.updateTable(restaurantId, tableId, request);
    }

    @Operation(
        summary = "Delete a table",
        description = "Soft deletes a table by marking it as inactive. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "204", description = "Table deleted successfully")
    @ApiResponse(responseCode = "404", description = "Table or restaurant not found")
    @DeleteMapping("/{tableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public void deleteTable(
            @Parameter(description = "ID of the restaurant") 
            @PathVariable Long restaurantId,
            @Parameter(description = "ID of the table to delete") 
            @PathVariable Long tableId) {
        tableService.deleteTable(restaurantId, tableId);
    }
} 