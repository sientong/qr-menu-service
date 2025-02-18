package com.qrmenu.controller;

import com.qrmenu.dto.menu.CategoryRequest;
import com.qrmenu.dto.menu.CategoryResponse;
import com.qrmenu.service.MenuCategoryService;
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
@RequestMapping("/api/v1/menus/{menuId}/categories")
@RequiredArgsConstructor
@Tag(name = "Menu Categories", description = "APIs for managing menu categories")
public class MenuCategoryController {

    private final MenuCategoryService categoryService;

    @Operation(
        summary = "Create a new category",
        description = "Creates a new menu category. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public CategoryResponse createCategory(
            @Parameter(description = "ID of the menu") 
            @PathVariable Long menuId,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.createCategory(menuId, request);
    }

    @Operation(
        summary = "Update a category",
        description = "Updates an existing menu category. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public CategoryResponse updateCategory(
            @Parameter(description = "ID of the menu") 
            @PathVariable Long menuId,
            @Parameter(description = "ID of the category") 
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(menuId, categoryId, request);
    }

    @Operation(
        summary = "Get menu categories",
        description = "Retrieves all active categories for a menu."
    )
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @GetMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_MANAGER')")
    public List<CategoryResponse> getCategories(
            @Parameter(description = "ID of the menu") 
            @PathVariable Long menuId) {
        return categoryService.getCategories(menuId);
    }

    @Operation(
        summary = "Reorder categories",
        description = "Updates the display order of categories. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Categories reordered successfully")
    @PostMapping("/reorder")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public void reorderCategories(
            @PathVariable Long menuId,
            @RequestBody List<Long> categoryIds) {
        categoryService.reorderCategories(menuId, categoryIds);
    }

    @Operation(
        summary = "Delete a category",
        description = "Soft deletes a category by marking it as inactive. Requires RESTAURANT_ADMIN role."
    )
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public void deleteCategory(
            @PathVariable Long menuId,
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(menuId, categoryId);
    }
} 