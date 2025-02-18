package com.qrmenu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.table.RestaurantTableRequest;
import com.qrmenu.dto.table.RestaurantTableResponse;
import com.qrmenu.service.RestaurantTableService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantTableController.class)
class RestaurantTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantTableService tableService;

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldCreateTable() throws Exception {
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);
        request.setDescription("Window seat");

        RestaurantTableResponse response = RestaurantTableResponse.builder()
                .id(1L)
                .tableNumber("T1")
                .capacity(4)
                .description("Window seat")
                .build();

        when(tableService.createTable(eq(1L), any(RestaurantTableRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/restaurants/1/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T1"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldGetRestaurantTables() throws Exception {
        RestaurantTableResponse table1 = RestaurantTableResponse.builder()
                .id(1L)
                .tableNumber("T1")
                .capacity(4)
                .build();

        RestaurantTableResponse table2 = RestaurantTableResponse.builder()
                .id(2L)
                .tableNumber("T2")
                .capacity(2)
                .build();

        when(tableService.getRestaurantTables(1L))
                .thenReturn(Arrays.asList(table1, table2));

        mockMvc.perform(get("/api/v1/restaurants/1/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldUpdateTable() throws Exception {
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1-Updated");
        request.setCapacity(6);

        RestaurantTableResponse response = RestaurantTableResponse.builder()
                .id(1L)
                .tableNumber("T1-Updated")
                .capacity(6)
                .build();

        when(tableService.updateTable(eq(1L), eq(1L), any(RestaurantTableRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/restaurants/1/tables/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber").value("T1-Updated"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldDeleteTable() throws Exception {
        mockMvc.perform(delete("/api/v1/restaurants/1/tables/1"))
                .andExpect(status().isNoContent());
    }
} 