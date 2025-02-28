package com.qrmenu.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.table.RestaurantTableRequest;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.RestaurantTable;
import com.qrmenu.repository.RestaurantRepository;
import com.qrmenu.repository.RestaurantTableRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RestaurantTableIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Test Restaurant")
                .active(true)
                .build());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldCreateAndRetrieveTable() throws Exception {
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);

        // Create table
        String responseJson = mockMvc.perform(post("/api/v1/restaurants/{id}/tables", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value("T1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify table was created in database
        RestaurantTable savedTable = tableRepository.findByRestaurantIdAndTableNumberAndActiveTrue(
                testRestaurant.getId(), "T1").orElseThrow();
        assertThat(savedTable.getCapacity()).isEqualTo(4);

        // Retrieve tables
        mockMvc.perform(get("/api/v1/restaurants/{id}/tables", testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tableNumber").value("T1"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldHandleTableNumberConflict() throws Exception {
        // Create first table
        RestaurantTable existingTable = tableRepository.save(RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build());

        // Try to create table with same number
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(6);

        mockMvc.perform(post("/api/v1/restaurants/{id}/tables", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldUpdateAndDeleteTable() throws Exception {
        // Create table
        RestaurantTable existingTable = tableRepository.save(RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build());

        // Update table
        RestaurantTableRequest updateRequest = new RestaurantTableRequest();
        updateRequest.setTableNumber("T1-Updated");
        updateRequest.setCapacity(6);

        mockMvc.perform(put("/api/v1/restaurants/{restaurantId}/tables/{tableId}",
                testRestaurant.getId(), existingTable.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber").value("T1-Updated"));

        // Delete table
        mockMvc.perform(delete("/api/v1/restaurants/{restaurantId}/tables/{tableId}",
                testRestaurant.getId(), existingTable.getId()))
                .andExpect(status().isNoContent());

        // Verify table is inactive
        assertThat(tableRepository.findById(existingTable.getId()))
                .hasValueSatisfying(table -> assertThat(table.isActive()).isFalse());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldAllowManagerToViewTables() throws Exception {
        // Create test table
        RestaurantTable existingTable = tableRepository.save(RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build());

        mockMvc.perform(get("/api/v1/restaurants/{id}/tables", testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tableNumber").value("T1"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_MANAGER")
    void shouldNotAllowManagerToCreateTable() throws Exception {
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);

        mockMvc.perform(post("/api/v1/restaurants/{id}/tables", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForAllEndpoints() throws Exception {
        // Test GET endpoint
        mockMvc.perform(get("/api/v1/restaurants/{id}/tables", testRestaurant.getId()))
                .andExpect(status().isUnauthorized());

        // Test POST endpoint
        mockMvc.perform(post("/api/v1/restaurants/{id}/tables", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());

        // Test PUT endpoint
        mockMvc.perform(put("/api/v1/restaurants/{id}/tables/1", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());

        // Test DELETE endpoint
        mockMvc.perform(delete("/api/v1/restaurants/{id}/tables/1", testRestaurant.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldValidateTableRequest() throws Exception {
        // Test empty table number
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setCapacity(4);

        mockMvc.perform(post("/api/v1/restaurants/{id}/tables", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Test invalid capacity
        request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(0);

        mockMvc.perform(post("/api/v1/restaurants/{id}/tables", testRestaurant.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldHandleNonExistentRestaurant() throws Exception {
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);

        mockMvc.perform(post("/api/v1/restaurants/999/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}