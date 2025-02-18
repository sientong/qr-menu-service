package com.qrmenu.dto.table;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantTableResponse {
    private Long id;
    private String tableNumber;
    private String description;
    private int capacity;
} 