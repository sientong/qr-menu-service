package com.qrmenu.repository;

import com.qrmenu.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    Optional<MenuCategory> findByIdAndActiveTrue(Long id);
    List<MenuCategory> findByMenuIdAndActiveTrueOrderByDisplayOrder(Long menuId);
    List<MenuCategory> findByMenuId(Long menuId);
} 