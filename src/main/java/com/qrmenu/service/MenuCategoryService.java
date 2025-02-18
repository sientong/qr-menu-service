package com.qrmenu.service;

import com.qrmenu.dto.menu.CategoryRequest;
import com.qrmenu.dto.menu.CategoryResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Menu;
import com.qrmenu.model.MenuCategory;
import com.qrmenu.repository.MenuCategoryRepository;
import com.qrmenu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for managing menu categories.
 * Provides functionality for creating, updating, and managing menu categories.
 */
@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    /**
     * Creates a new menu category.
     *
     * @param menuId ID of the menu
     * @param request Category creation request
     * @return Created category response
     * @throws ResourceNotFoundException if menu not found
     */
    @Transactional
    public CategoryResponse createCategory(Long menuId, CategoryRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));

        MenuCategory category = MenuCategory.builder()
                .menu(menu)
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    /**
     * Updates an existing menu category.
     *
     * @param menuId ID of the menu
     * @param categoryId ID of the category to update
     * @param request Update request containing new category details
     * @return Updated category response
     * @throws ResourceNotFoundException if category not found
     */
    @Transactional
    public CategoryResponse updateCategory(Long menuId, Long categoryId, CategoryRequest request) {
        MenuCategory category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("Category does not belong to the menu");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    /**
     * Retrieves all active categories for a menu.
     *
     * @param menuId ID of the menu
     * @return List of category responses
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long menuId) {
        return categoryRepository.findByMenuIdAndActiveTrueOrderByDisplayOrder(menuId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates the display order of categories.
     *
     * @param menuId ID of the menu
     * @param categoryIds List of category IDs in the desired order
     */
    @Transactional
    public void reorderCategories(Long menuId, List<Long> categoryIds) {
        List<MenuCategory> categories = categoryIds.stream()
                .map(id -> categoryRepository.findByIdAndActiveTrue(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found")))
                .collect(Collectors.toList());

        IntStream.range(0, categories.size())
                .forEach(i -> categories.get(i).setDisplayOrder(i + 1));

        categoryRepository.saveAll(categories);
    }

    /**
     * Soft deletes a category by marking it as inactive.
     *
     * @param menuId ID of the menu
     * @param categoryId ID of the category to delete
     * @throws ResourceNotFoundException if category not found
     */
    @Transactional
    public void deleteCategory(Long menuId, Long categoryId) {
        MenuCategory category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getMenu().getId().equals(menuId)) {
            throw new IllegalArgumentException("Category does not belong to the menu");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(MenuCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
} 