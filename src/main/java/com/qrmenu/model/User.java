package com.qrmenu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private boolean active = true;  // Default to true for new users

    /**
     * Check if the user has access to the specified restaurant
     * @param restaurantId ID of the restaurant to check access for
     * @return true if user has access, false otherwise
     */
    public boolean hasAccessToRestaurant(Long restaurantId) {
        // SUPER_ADMIN has access to all restaurants
        if (role == UserRole.SUPER_ADMIN) {
            return true;
        }
        
        // For other roles, check if they belong to the restaurant
        return restaurant != null && restaurant.getId().equals(restaurantId);
    }
}