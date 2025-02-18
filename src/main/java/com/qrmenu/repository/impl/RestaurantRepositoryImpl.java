package com.qrmenu.repository.impl;

import com.qrmenu.model.Restaurant;
import com.qrmenu.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Restaurant> restaurantRowMapper = (rs, rowNum) -> Restaurant.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .website(rs.getString("website"))
            .phone(rs.getString("phone"))
            .address(rs.getString("address"))
            .logoUrl(rs.getString("logo_url"))
            .active(rs.getBoolean("is_active"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    @Override
    public Restaurant save(Restaurant restaurant) {
        if (restaurant.getId() == null) {
            return insert(restaurant);
        }
        return update(restaurant);
    }

    private Restaurant insert(Restaurant restaurant) {
        String sql = "INSERT INTO restaurants (name, description, website, phone, address, logo_url, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, restaurant.getName());
            ps.setString(2, restaurant.getDescription());
            ps.setString(3, restaurant.getWebsite());
            ps.setString(4, restaurant.getPhone());
            ps.setString(5, restaurant.getAddress());
            ps.setString(6, restaurant.getLogoUrl());
            ps.setBoolean(7, restaurant.isActive());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated ID");
        }
        restaurant.setId(key.longValue());
        return restaurant;
    }

    private Restaurant update(Restaurant restaurant) {
        String sql = "UPDATE restaurants SET name = ?, description = ?, website = ?, phone = ?, " +
                "address = ?, logo_url = ?, is_active = ?, updated_at = ? WHERE id = ?";
        
        jdbcTemplate.update(sql,
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getWebsite(),
                restaurant.getPhone(),
                restaurant.getAddress(),
                restaurant.getLogoUrl(),
                restaurant.isActive(),
                Timestamp.valueOf(restaurant.getUpdatedAt()),
                restaurant.getId());
        return restaurant;
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        return jdbcTemplate.query(sql, restaurantRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Restaurant> findAll() {
        String sql = "SELECT * FROM restaurants ORDER BY name";
        return jdbcTemplate.query(sql, restaurantRowMapper);
    }

    @Override
    public List<Restaurant> findAllActive() {
        String sql = "SELECT * FROM restaurants WHERE is_active = true ORDER BY name";
        return jdbcTemplate.query(sql, restaurantRowMapper);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM restaurants WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM restaurants WHERE name = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count > 0;
    }
} 