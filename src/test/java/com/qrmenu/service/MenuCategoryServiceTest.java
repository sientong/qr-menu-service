package com.qrmenu.service;

import com.qrmenu.dto.menu.CategoryRequest;
import com.qrmenu.dto.menu.CategoryResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Menu;
import com.qrmenu.model.MenuCategory;
import com.qrmenu.repository.MenuCategoryRepository;
import com.qrmenu.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuCategoryServiceTest {

    @Mock
    private MenuCategoryRepository categoryRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuCategoryService categoryService;

    @Test
    void shouldCreateCategory() {
        Menu menu = Menu.builder()
                .id(1L)
                .build();

        CategoryRequest request = new CategoryRequest();
        request.setName("Main Course");
        request.setDisplayOrder(1);

        when(menuRepository.findById(1L))
                .thenReturn(Optional.of(menu));
        when(categoryRepository.save(any(MenuCategory.class)))
                .thenAnswer(i -> {
                    MenuCategory category = i.getArgument(0);
                    category.setId(1L);
                    return category;
                });

        CategoryResponse response = categoryService.createCategory(1L, request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Main Course");
        verify(categoryRepository).save(any(MenuCategory.class));
    }

    @Test
    void shouldUpdateCategory() {
        Menu menu = Menu.builder()
                .id(1L)
                .build();

        MenuCategory existingCategory = MenuCategory.builder()
                .id(1L)
                .menu(menu)
                .name("Old Name")
                .displayOrder(1)
                .active(true)
                .build();

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Name");
        request.setDisplayOrder(2);

        when(categoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(MenuCategory.class)))
                .thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.updateCategory(1L, 1L, request);

        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void shouldGetCategories() {
        MenuCategory category1 = MenuCategory.builder()
                .id(1L)
                .name("Category 1")
                .displayOrder(1)
                .active(true)
                .build();

        MenuCategory category2 = MenuCategory.builder()
                .id(2L)
                .name("Category 2")
                .displayOrder(2)
                .active(true)
                .build();

        when(categoryRepository.findByMenuIdAndActiveTrueOrderByDisplayOrder(1L))
                .thenReturn(Arrays.asList(category1, category2));

        List<CategoryResponse> responses = categoryService.getCategories(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void shouldReorderCategories() {
        MenuCategory category1 = MenuCategory.builder()
                .id(1L)
                .displayOrder(1)
                .build();

        MenuCategory category2 = MenuCategory.builder()
                .id(2L)
                .displayOrder(2)
                .build();

        when(categoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(category1));
        when(categoryRepository.findByIdAndActiveTrue(2L))
                .thenReturn(Optional.of(category2));

        categoryService.reorderCategories(1L, Arrays.asList(2L, 1L));

        verify(categoryRepository).saveAll(argThat(categories -> {
            List<MenuCategory> list = (List<MenuCategory>) categories;
            return list.get(0).getDisplayOrder() == 2 
                && list.get(1).getDisplayOrder() == 1;
        }));
    }

    @Test
    void shouldDeleteCategory() {
        Menu menu = Menu.builder()
                .id(1L)
                .build();

        MenuCategory category = MenuCategory.builder()
                .id(1L)
                .menu(menu)
                .active(true)
                .build();

        when(categoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L, 1L);

        verify(categoryRepository).save(argThat(cat -> 
            !((MenuCategory) cat).isActive()
        ));
    }

    @Test
    void shouldThrowExceptionWhenMenuNotFound() {
        when(menuRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            categoryService.createCategory(1L, new CategoryRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Menu not found");
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            categoryService.updateCategory(1L, 1L, new CategoryRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
    }
} 