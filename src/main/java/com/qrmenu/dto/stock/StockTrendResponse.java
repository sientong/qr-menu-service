package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Stock trend analysis response")
public class StockTrendResponse {
    @Schema(description = "Menu item ID")
    private Long menuItemId;

    @Schema(description = "Menu item name")
    private String menuItemName;

    @Schema(description = "Current stock quantity")
    private Integer currentQuantity;

    @Schema(description = "Average stock quantity over period")
    private Double averageQuantity;

    @Schema(description = "Maximum stock quantity over period")
    private Integer maxQuantity;

    @Schema(description = "Minimum stock quantity over period")
    private Integer minQuantity;

    @Schema(description = "Number of stock adjustments")
    private Integer adjustmentCount;
} 