package com.qrmenu.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String tableNumber;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int capacity;
} 