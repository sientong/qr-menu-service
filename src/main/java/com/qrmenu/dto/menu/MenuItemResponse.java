package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Menu item response")
public class MenuItemResponse {
    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "Menu Category ID", example = "1")
    private Long menuCategoryId;

    @Schema(description = "Item name", example = "Margherita Pizza")
    private String name;

    @Schema(description = "Item description", example = "Fresh tomatoes, mozzarella, and basil")
    private String description;

    @Schema(description = "Price", example = "12.99")
    private BigDecimal price;

    @Schema(description = "Image URL", example = "https://example.com/images/pizza.jpg")
    private String imageUrl;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Active status", example = "true")
    private boolean active;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}