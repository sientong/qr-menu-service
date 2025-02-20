package com.qrmenu.service;

import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.dto.menu.MenuItemResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.MenuCategory;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.MenuCategoryRepository;
import com.qrmenu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        MenuCategory category = menuCategoryRepository.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Validate menu item name uniqueness within category
        if (menuItemRepository.existsByCategoryIdAndNameAndActiveTrue(
                request.getCategoryId(), request.getName())) {
            throw new IllegalArgumentException(
                    "Menu item with name '" + request.getName() + "' already exists in this category");
        }

        MenuItem menuItem = MenuItem.builder()
                .category(category)
                .menu(category.getMenu())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(Long categoryId, boolean activeOnly) {
        List<MenuItem> items = activeOnly ?
                menuItemRepository.findByCategoryIdAndActiveOrderByDisplayOrderAsc(categoryId, true) :
                menuItemRepository.findByCategoryIdOrderByDisplayOrderAsc(categoryId);

        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        return mapToResponse(menuItem);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long menuId, Long itemId, MenuItemRequest request) {
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        validateMenuItemBelongsToCategory(menuId, itemId);

        // Validate menu item name uniqueness (excluding current item)
        if (!menuItem.getName().equals(request.getName()) &&
            menuItemRepository.existsByCategoryIdAndNameAndActiveTrueAndIdNot(
                menuItem.getCategory().getId(), request.getName(), itemId)) {
            throw new IllegalArgumentException(
                    "Menu item with name '" + request.getName() + "' already exists in this category");
        }

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setDisplayOrder(request.getDisplayOrder());

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Transactional
    public void deleteMenuItem(Long menuId, Long itemId) {
        MenuItem menuItem = menuItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        validateMenuItemBelongsToCategory(menuId, itemId);

        // Soft delete by setting active to false
        menuItem.setActive(false);
        menuItemRepository.save(menuItem);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .menuCategoryId(menuItem.getCategory().getId())
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

    private void validateMenuItemBelongsToCategory(Long menuId, Long itemId) {
        MenuItem item = menuItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getCategory().getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("Menu item does not belong to the menu");
        }
    }
}