package com.qrmenu.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.model.Menu;
import com.qrmenu.model.MenuCategory;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.Restaurant;
import com.qrmenu.repository.MenuCategoryRepository;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.MenuRepository;
import com.qrmenu.repository.RestaurantRepository;

@Transactional
class MenuIntegrationTest extends IntegrationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Restaurant testRestaurant;
    private Menu testMenu;
    private MenuCategory testCategory;

    @BeforeEach
    void setUp() {
        testRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Test Restaurant")
                .active(true)
                .build());

        testMenu = menuRepository.save(Menu.builder()
                .restaurant(testRestaurant)
                .name("Main Menu")
                .active(true)
                .build());

        testCategory = menuCategoryRepository.save(MenuCategory.builder()
                .restaurant(testRestaurant)
                .menu(testMenu)
                .name("Test Category")
                .displayOrder(1)
                .active(true)
                .build());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldCreateAndUpdateMenuItem() throws Exception {

        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setDescription("Test Description");
        request.setPrice(BigDecimal.valueOf(9.99));
        request.setMenuId(testMenu.getId());
        request.setCategoryId(testCategory.getId());
        request.setImageUrl("http://example.com/images/test-image.jpg");

        String responseJson = mockMvc.perform(post("/api/v1/menus/{menuId}/items", testMenu.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.imageUrl").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long itemId = objectMapper.readTree(responseJson).get("id").asLong();

        // Update menu item
        request.setName("Updated Item");
        request.setPrice(BigDecimal.valueOf(12.99));

        mockMvc.perform(put("/api/v1/menus/{menuId}/items/{itemId}", testMenu.getId(), itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.price").value(12.99));

        // Verify in database
        MenuItem savedItem = menuItemRepository.findById(itemId).orElseThrow();
        assertThat(savedItem.getName()).isEqualTo("Updated Item");
        assertThat(savedItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(12.99));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldHandleMenuItemAvailability() throws Exception {
        MenuItem item = menuItemRepository.save(MenuItem.builder()
                .menu(testMenu)
                .category(testCategory)
                .name("Test Item")
                .price(BigDecimal.valueOf(9.99))
                .active(true)
                .build());

        // Set item as inactive
        mockMvc.perform(patch("/api/v1/menus/{menuId}/items/{itemId}/availability",
                testMenu.getId(), item.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Verify in database
        MenuItem updatedItem = menuItemRepository.findById(item.getId()).orElseThrow();
        assertThat(updatedItem.isActive()).isFalse();
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldAllowManagerToUpdateAvailability() throws Exception {
        MenuItem item = menuItemRepository.save(MenuItem.builder()
                .menu(testMenu)
                .category(testCategory)
                .name("Test Item")
                .price(BigDecimal.valueOf(9.99))
                .active(true)
                .build());

        mockMvc.perform(patch("/api/v1/menus/{menuId}/items/{itemId}/availability",
                testMenu.getId(), item.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": false}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldNotAllowManagerToCreateOrUpdateItems() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setPrice(BigDecimal.valueOf(9.99));
        request.setMenuId(testMenu.getId());
        request.setCategoryId(testCategory.getId());

        mockMvc.perform(post("/api/v1/menus/{menuId}/items", testMenu.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForMenuOperations() throws Exception {
        mockMvc.perform(get("/api/v1/menus/{menuId}/items", testMenu.getId()))
                .andExpect(status().isUnauthorized());
    }
}