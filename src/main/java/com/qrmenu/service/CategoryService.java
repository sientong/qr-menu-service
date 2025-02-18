package com.qrmenu.service;

import com.qrmenu.dto.menu.CategoryRequest;
import com.qrmenu.dto.menu.CategoryResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Category;
import com.qrmenu.model.Restaurant;
import com.qrmenu.repository.CategoryRepository;
import com.qrmenu.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public CategoryResponse createCategory(Long restaurantId, CategoryRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Category category = Category.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(request.isActive())
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long restaurantId, boolean activeOnly) {
        List<Category> categories = activeOnly ?
                categoryRepository.findByRestaurantIdAndActiveOrderByDisplayOrderAsc(restaurantId, true) :
                categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setActive(request.isActive());

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .restaurantId(category.getRestaurant().getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
} 