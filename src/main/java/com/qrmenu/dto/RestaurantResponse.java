package com.qrmenu.dto;

import com.qrmenu.model.Restaurant;
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.ZoneId;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String website;
    private String phone;
    private String address;
    private String logoUrl;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public static RestaurantResponse from(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setDescription(restaurant.getDescription());
        response.setWebsite(restaurant.getWebsite());
        response.setPhone(restaurant.getPhone());
        response.setAddress(restaurant.getAddress());
        response.setLogoUrl(restaurant.getLogoUrl());
        response.setActive(restaurant.isActive());
        response.setCreatedAt(restaurant.getCreatedAt().atZone(ZoneId.systemDefault()));
        response.setUpdatedAt(restaurant.getUpdatedAt().atZone(ZoneId.systemDefault()));
        return response;
    }
} 