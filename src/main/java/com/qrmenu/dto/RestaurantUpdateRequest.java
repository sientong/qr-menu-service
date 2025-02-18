package com.qrmenu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RestaurantUpdateRequest {
    @NotNull(message = "Restaurant ID is required")
    private Long id;
    
    private String name;
    private String description;
    
    @Pattern(regexp = "^(https?://)?[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*$", message = "Invalid website URL format")
    private String website;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    private String address;
    private String logoUrl;
    private Boolean active;
} 