package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Category response")
public class CategoryResponse {
    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Restaurant ID", example = "1")
    private Long restaurantId;

    @Schema(description = "Category name", example = "Main Course")
    private String name;

    @Schema(description = "Category description", example = "Our signature main dishes")
    private String description;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Active status", example = "true")
    private boolean active;

    @Schema(description = "Menu items in this category")
    private List<MenuItemResponse> menuItems;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Item count", example = "5")
    private int itemCount;
} 