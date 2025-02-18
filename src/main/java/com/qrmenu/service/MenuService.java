package com.qrmenu.service;

import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.dto.menu.MenuItemResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Menu;
import com.qrmenu.model.MenuItem;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Service for managing restaurant menus and menu items.
 * Provides functionality for creating, updating, and managing menu items,
 * including image handling and availability management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Creates a new menu item with optional image.
     *
     * @param menuId ID of the menu
     * @param request Menu item creation request
     * @return Created menu item response
     * @throws ResourceNotFoundException if menu not found
     * @throws IllegalArgumentException if request data is invalid
     */
    @Transactional
    public MenuItemResponse createMenuItem(Long menuId, MenuItemRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));

        MenuItem menuItem = MenuItem.builder()
                .menu(menu)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .active(request.isActive())
                .build();

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    /**
     * Updates an existing menu item.
     *
     * @param menuId ID of the menu
     * @param itemId ID of the menu item to update
     * @param request Update request containing new item details
     * @return Updated menu item response
     * @throws ResourceNotFoundException if menu item not found
     * @throws IllegalArgumentException if request data is invalid
     */
    @Transactional
    public MenuItemResponse updateMenuItem(Long menuId, Long itemId, MenuItemRequest request) {
        // ... existing implementation ...
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice()); 
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setDisplayOrder(request.getDisplayOrder());
        menuItem.setActive(request.isActive());

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    /**
     * Updates the availability status of a menu item.
     * This operation is allowed for both ADMIN and MANAGER roles.
     *
     * @param menuId ID of the menu
     * @param itemId ID of the menu item
     * @param available New availability status
     * @return Updated menu item response
     * @throws ResourceNotFoundException if menu item not found
     */
    @Transactional
    public MenuItemResponse updateItemAvailability(Long menuId, Long itemId, boolean available) {
        // ... existing implementation ...
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        menuItem.setAvailable(available);
        menuItemRepository.save(menuItem);

        return mapToResponse(menuItem);
    }

    /**
     * Retrieves all active menu items for a menu.
     *
     * @param menuId ID of the menu
     * @return List of menu item responses
     */
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(Long menuId) {
        List<MenuItem> menuItems = menuItemRepository.findByMenuId(menuId);
        return menuItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Soft deletes a menu item by marking it as inactive.
     *
     * @param menuId ID of the menu
     * @param itemId ID of the menu item to delete
     * @throws ResourceNotFoundException if menu item not found
     */
    @Transactional
    public void deleteMenuItem(Long menuId, Long itemId) {
        // ... existing implementation ...
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        menuItem.setActive(false);
        menuItemRepository.save(menuItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .imageUrl(menuItem.getImageUrl())
                .displayOrder(menuItem.getDisplayOrder())
                .active(menuItem.isActive())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .build();
    }
} 