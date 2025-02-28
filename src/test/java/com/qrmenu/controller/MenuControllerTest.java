package com.qrmenu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.dto.menu.MenuItemResponse;
import com.qrmenu.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldCreateMenuItem() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setPrice(BigDecimal.valueOf(9.99));

        MenuItemResponse response = MenuItemResponse.builder()
                .id(1L)
                .name("Test Item")
                .price(BigDecimal.valueOf(9.99))
                .build();

        when(menuService.createMenuItem(eq(1L), any(MenuItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/menus/{menuId}/items", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldUpdateMenuItem() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Updated Item");
        request.setPrice(BigDecimal.valueOf(12.99));

        MenuItemResponse response = MenuItemResponse.builder()
                .id(1L)
                .name("Updated Item")
                .price(BigDecimal.valueOf(12.99))
                .build();

        when(menuService.updateMenuItem(eq(1L), eq(1L), any(MenuItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/menus/{menuId}/items/{itemId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldUpdateItemAvailability() throws Exception {
        MenuItemResponse response = MenuItemResponse.builder()
                .id(1L)
                .active(false)
                .build();

        when(menuService.updateItemAvailability(eq(1L), eq(1L), eq(false)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/menus/{menuId}/items/{itemId}/availability", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldGetMenuItems() throws Exception {
        MenuItemResponse item1 = MenuItemResponse.builder()
                .id(1L)
                .name("Item 1")
                .build();

        MenuItemResponse item2 = MenuItemResponse.builder()
                .id(2L)
                .name("Item 2")
                .build();

        when(menuService.getMenuItems(1L))
                .thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/v1/menus/{menuId}/items", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldDeleteMenuItem() throws Exception {
        mockMvc.perform(delete("/api/v1/menus/{menuId}/items/{itemId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(menuService).deleteMenuItem(1L, 1L);
    }

    @Test
    void shouldRequireAuthenticationForAllEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/menus/1/items"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/menus/1/items"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/menus/1/items/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/menus/1/items/1"))
                .andExpect(status().isUnauthorized());
    }
} 