package com.qrmenu.service;

import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.dto.menu.MenuItemResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.MenuItem;
import com.qrmenu.repository.MenuCategoryRepository;
import com.qrmenu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.qrmenu.model.Menu;
import com.qrmenu.repository.MenuRepository;
import com.qrmenu.model.MenuCategory;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRepository menuRepository;
    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        MenuCategory menuCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu Category not found"));

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));

        MenuItem menuItem = MenuItem.builder()
                .menu(menu)
                .menuCategory(menuCategory)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .active(request.isActive())
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
        return menuItemRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setDisplayOrder(request.getDisplayOrder());
        menuItem.setActive(request.isActive());

        return mapToResponse(menuItemRepository.save(menuItem));
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu item not found");
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .menuCategoryId(menuItem.getMenuCategory().getId())
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