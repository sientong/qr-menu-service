package com.qrmenu.service;

import com.qrmenu.dto.table.RestaurantTableRequest;
import com.qrmenu.dto.table.RestaurantTableResponse;
import com.qrmenu.exception.ResourceNotFoundException;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.RestaurantTable;
import com.qrmenu.repository.RestaurantRepository;
import com.qrmenu.repository.RestaurantTableRepository;
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
class RestaurantTableServiceTest {

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantTableService tableService;

    @Test
    void shouldCreateTable() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .build();

        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");
        request.setCapacity(4);

        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));
        when(tableRepository.existsByRestaurantIdAndTableNumberAndActiveTrue(1L, "T1"))
                .thenReturn(false);
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(i -> i.getArgument(0));

        var response = tableService.createTable(1L, request);

        assertThat(response.getTableNumber()).isEqualTo("T1");
        assertThat(response.getCapacity()).isEqualTo(4);
        verify(tableRepository).save(any(RestaurantTable.class));
    }

    @Test
    void shouldThrowExceptionWhenTableNumberExists() {
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1");

        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(Restaurant.builder().id(1L).build()));
        when(tableRepository.existsByRestaurantIdAndTableNumberAndActiveTrue(1L, "T1"))
                .thenReturn(true);

        assertThatThrownBy(() -> tableService.createTable(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Table number already exists");
    }

    @Test
    void shouldThrowExceptionWhenRestaurantNotFound() {
        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.createTable(1L, new RestaurantTableRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Restaurant not found");
    }

    @Test
    void shouldGetRestaurantTables() {
        RestaurantTable table1 = RestaurantTable.builder()
                .id(1L)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build();

        RestaurantTable table2 = RestaurantTable.builder()
                .id(2L)
                .tableNumber("T2")
                .capacity(2)
                .active(true)
                .build();

        when(tableRepository.findByRestaurantIdAndActiveTrue(1L))
                .thenReturn(Arrays.asList(table1, table2));

        List<RestaurantTableResponse> responses = tableService.getRestaurantTables(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTableNumber()).isEqualTo("T1");
        assertThat(responses.get(1).getTableNumber()).isEqualTo("T2");
    }

    @Test
    void shouldUpdateTable() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build();

        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T1-Updated");
        request.setCapacity(6);

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(existingTable));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(i -> i.getArgument(0));

        var response = tableService.updateTable(1L, 1L, request);

        assertThat(response.getTableNumber()).isEqualTo("T1-Updated");
        assertThat(response.getCapacity()).isEqualTo(6);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTableFromDifferentRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .id(2L)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(1L)
                .restaurant(restaurant)
                .build();

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(existingTable));

        assertThatThrownBy(() -> 
            tableService.updateTable(1L, 1L, new RestaurantTableRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Table does not belong to the restaurant");
    }

    @Test
    void shouldDeleteTable() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(1L)
                .restaurant(restaurant)
                .active(true)
                .build();

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(existingTable));

        tableService.deleteTable(1L, 1L);

        verify(tableRepository).save(argThat(table -> 
            !table.isActive()
        ));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTable() {
        when(tableRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            tableService.updateTable(1L, 1L, new RestaurantTableRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Table not found");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingTableNumber() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("T1")
                .active(true)
                .build();

        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setTableNumber("T2");

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(existingTable));
        when(tableRepository.existsByRestaurantIdAndTableNumberAndActiveTrue(1L, "T2"))
                .thenReturn(true);

        assertThatThrownBy(() -> 
            tableService.updateTable(1L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Table number already exists");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTable() {
        when(tableRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> 
            tableService.deleteTable(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Table not found");
    }

    @Test
    void shouldThrowExceptionWhenDeletingTableFromDifferentRestaurant() {
        Restaurant restaurant = Restaurant.builder()
                .id(2L)
                .build();

        RestaurantTable existingTable = RestaurantTable.builder()
                .id(1L)
                .restaurant(restaurant)
                .active(true)
                .build();

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(existingTable));

        assertThatThrownBy(() -> 
            tableService.deleteTable(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Table does not belong to the restaurant");
    }
} 