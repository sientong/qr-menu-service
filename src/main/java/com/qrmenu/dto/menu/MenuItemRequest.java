package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Menu item creation/update request")
public class MenuItemRequest {
    @Schema(description = "Menu ID", example = "1")
    @NotNull(message = "Menu ID is required")
    private Long menuId;

    @Schema(description = "Menu Category ID", example = "1")
    @NotNull(message = "Menu Category ID is required")
    private Long menuCategoryId;

    @Schema(description = "Item name", example = "Margherita Pizza")
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Item description", example = "Fresh tomatoes, mozzarella, and basil")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Item price", example = "12.99")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price must not exceed 99999.99")
    private BigDecimal price;

    @Schema(description = "Image URL", example = "https://example.com/images/pizza.jpg")
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;

    @Schema(description = "Item visibility status", example = "true")
    private boolean active = true;
} 