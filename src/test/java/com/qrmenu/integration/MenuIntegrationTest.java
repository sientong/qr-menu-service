package com.qrmenu.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.menu.MenuItemRequest;
import com.qrmenu.model.Menu;
import com.qrmenu.model.MenuItem;
import com.qrmenu.model.Restaurant;
import com.qrmenu.repository.MenuItemRepository;
import com.qrmenu.repository.MenuRepository;
import com.qrmenu.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MenuIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private Restaurant testRestaurant;
    private Menu testMenu;

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
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldCreateAndUpdateMenuItem() throws Exception {
        // Create menu item with image
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setDescription("Test Description");
        request.setPrice(BigDecimal.valueOf(9.99));
        request.setImageBase64(Base64.getEncoder().encodeToString(imageFile.getBytes()));

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
                .name("Test Item")
                .price(BigDecimal.valueOf(9.99))
                .available(true)
                .active(true)
                .build());

        // Set item as unavailable
        mockMvc.perform(patch("/api/v1/menus/{menuId}/items/{itemId}/availability", 
                testMenu.getId(), item.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        // Verify in database
        MenuItem updatedItem = menuItemRepository.findById(item.getId()).orElseThrow();
        assertThat(updatedItem.isAvailable()).isFalse();
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldAllowManagerToUpdateAvailability() throws Exception {
        MenuItem item = menuItemRepository.save(MenuItem.builder()
                .menu(testMenu)
                .name("Test Item")
                .price(BigDecimal.valueOf(9.99))
                .available(true)
                .active(true)
                .build());

        mockMvc.perform(patch("/api/v1/menus/{menuId}/items/{itemId}/availability", 
                testMenu.getId(), item.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\": false}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldNotAllowManagerToCreateOrUpdateItems() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setPrice(BigDecimal.valueOf(9.99));

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