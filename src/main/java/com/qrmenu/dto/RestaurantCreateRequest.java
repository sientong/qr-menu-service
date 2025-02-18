package com.qrmenu.dto;

import com.qrmenu.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RestaurantCreateRequest {
    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Restaurant name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @ValidUrl
    private String website;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;
    
    @ValidUrl
    private String logoUrl;
    
    private boolean active = true;
} 