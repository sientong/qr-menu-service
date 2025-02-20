package com.qrmenu.service;

import com.qrmenu.dto.menu.CategoryRequest;
import com.qrmenu.dto.menu.CategoryResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Category;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.User;
import com.qrmenu.repository.CategoryRepository;
import com.qrmenu.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;

    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Transactional
    public CategoryResponse createCategory(Long restaurantId, CategoryRequest request) {
        if (request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        // Validate category name uniqueness within restaurant
        if (categoryRepository.existsByRestaurantIdAndNameAndActiveTrue(restaurantId, request.getName())) {
            throw new IllegalArgumentException(
                    "Category with name '" + request.getName() + "' already exists in this restaurant");
        }

        Category category = Category.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(true)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long restaurantId, boolean activeOnly) {
        validateRestaurantAccess(restaurantId);

        List<Category> categories = activeOnly ?
                categoryRepository.findByRestaurantIdAndActiveOrderByDisplayOrderAsc(restaurantId, true) :
                categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        validateRestaurantAccess(category.getRestaurant().getId());
        
        return mapToResponse(category);
    }

    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        if (request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateRestaurantAccess(category.getRestaurant().getId());

        // Check name uniqueness only if name is being changed
        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByRestaurantIdAndNameAndActiveTrue(
                category.getRestaurant().getId(), request.getName())) {
            throw new IllegalArgumentException(
                    "Category with name '" + request.getName() + "' already exists in this restaurant");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());

        return mapToResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        validateRestaurantAccess(category.getRestaurant().getId());
        
        // Soft delete by setting active to false
        category.setActive(false);
        categoryRepository.save(category);
    }

    private void validateRestaurantAccess(Long restaurantId) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.hasAccessToRestaurant(restaurantId)) {
            throw new AccessDeniedException("You don't have access to this restaurant");
        }
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