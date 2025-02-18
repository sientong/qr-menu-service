package com.qrmenu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.menu.CategoryRequest;
import com.qrmenu.dto.menu.CategoryResponse;
import com.qrmenu.service.MenuCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuCategoryController.class)
class MenuCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuCategoryService categoryService;

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Main Course");
        request.setDisplayOrder(1);

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Main Course")
                .displayOrder(1)
                .build();

        when(categoryService.createCategory(eq(1L), any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/menus/{menuId}/categories", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Main Course"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldUpdateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category");
        request.setDisplayOrder(2);

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Updated Category")
                .displayOrder(2)
                .build();

        when(categoryService.updateCategory(eq(1L), eq(1L), any(CategoryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/menus/{menuId}/categories/{categoryId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldGetCategories() throws Exception {
        CategoryResponse category1 = CategoryResponse.builder()
                .id(1L)
                .name("Category 1")
                .displayOrder(1)
                .build();

        CategoryResponse category2 = CategoryResponse.builder()
                .id(2L)
                .name("Category 2")
                .displayOrder(2)
                .build();

        when(categoryService.getCategories(1L))
                .thenReturn(Arrays.asList(category1, category2));

        mockMvc.perform(get("/api/v1/menus/{menuId}/categories", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldReorderCategories() throws Exception {
        mockMvc.perform(post("/api/v1/menus/{menuId}/categories/reorder", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[1, 2, 3]"))
                .andExpect(status().isOk());

        verify(categoryService).reorderCategories(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/v1/menus/{menuId}/categories/{categoryId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L, 1L);
    }

    @Test
    void shouldRequireAuthenticationForAllEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/menus/1/categories"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/menus/1/categories"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/menus/1/categories/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/menus/1/categories/1"))
                .andExpect(status().isUnauthorized());
    }
} 