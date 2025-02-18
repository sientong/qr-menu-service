package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Stock information response")
public class StockResponse {
    @Schema(description = "Menu item ID", example = "1")
    private Long menuItemId;

    @Schema(description = "Current stock quantity", example = "10")
    private Integer stockQuantity;

    @Schema(description = "Low stock threshold", example = "5")
    private Integer lowStockThreshold;

    @Schema(description = "Whether stock tracking is enabled", example = "true")
    private boolean trackStock;

    @Schema(description = "Whether the item is active", example = "true")
    private boolean active;

    @Schema(description = "Whether stock is low", example = "false")
    private boolean lowStock;
} 