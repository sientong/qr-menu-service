package com.qrmenu.controller;

import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.dto.menu.MenuItemResponse;
import com.qrmenu.service.MenuService;
import com.qrmenu.dto.menu.AvailabilityRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus/{menuId}/items")
@RequiredArgsConstructor
@Tag(name = "Menu Items", description = "APIs for managing menu items")
public class MenuController {

    private final MenuService menuService;

    @Operation(
        summary = "Create a new menu item",
        description = "Creates a new menu item with optional image. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "201", description = "Menu item created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public MenuItemResponse createMenuItem(
            @Parameter(description = "ID of the menu") 
            @PathVariable Long menuId,
            @Valid @RequestBody MenuItemRequest request) {
        return menuService.createMenuItem(menuId, request);
    }

    @Operation(
        summary = "Update a menu item",
        description = "Updates an existing menu item. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Menu item updated successfully")
    @ApiResponse(responseCode = "404", description = "Menu item not found")
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public MenuItemResponse updateMenuItem(
            @Parameter(description = "ID of the menu") 
            @PathVariable Long menuId,
            @Parameter(description = "ID of the menu item") 
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return menuService.updateMenuItem(menuId, itemId, request);
    }

    @Operation(
        summary = "Update menu item availability",
        description = "Updates the availability status of a menu item. Allowed for ADMIN and MANAGER roles."
    )
    @ApiResponse(responseCode = "200", description = "Availability updated successfully")
    @PatchMapping("/{itemId}/availability")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    public MenuItemResponse updateItemAvailability(
            @PathVariable Long menuId,
            @PathVariable Long itemId,
            @RequestBody AvailabilityRequest request) {
        return menuService.updateItemAvailability(menuId, itemId, request.isAvailable());
    }

    @Operation(
        summary = "Get menu items",
        description = "Retrieves all active menu items for a menu."
    )
    @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully")
    @GetMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    public List<MenuItemResponse> getMenuItems(
            @Parameter(description = "ID of the menu") 
            @PathVariable Long menuId) {
        return menuService.getMenuItems(menuId);
    }

    @Operation(
        summary = "Delete a menu item",
        description = "Soft deletes a menu item by marking it as inactive. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "204", description = "Menu item deleted successfully")
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public void deleteMenuItem(
            @PathVariable Long menuId,
            @PathVariable Long itemId) {
        menuService.deleteMenuItem(menuId, itemId);
    }
} 