package com.qrmenu.repository;

import com.qrmenu.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    Optional<MenuCategory> findByIdAndActiveTrue(Long id);
    List<MenuCategory> findByMenuIdAndActiveTrueOrderByDisplayOrder(Long menuId);
    List<MenuCategory> findByMenuId(Long menuId);
    
    // Check if a category with the given name exists in the menu
    boolean existsByMenuIdAndNameAndActiveTrue(Long menuId, String name);
    
    // Check if a category with the given name exists in the menu, excluding a specific category
    @Query("SELECT COUNT(c) > 0 FROM MenuCategory c " +
           "WHERE c.menu.id = :menuId " +
           "AND c.name = :name " +
           "AND c.active = true " +
           "AND c.id != :excludeCategoryId")
    boolean existsByMenuIdAndNameAndActiveTrueAndIdNot(
            @Param("menuId") Long menuId,
            @Param("name") String name,
            @Param("excludeCategoryId") Long excludeCategoryId);
            
    // Find active categories by menu ID and category IDs
    List<MenuCategory> findByMenuIdAndActiveTrueAndIdIn(Long menuId, List<Long> ids);
}