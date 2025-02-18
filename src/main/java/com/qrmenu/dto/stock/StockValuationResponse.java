package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Stock valuation response")
public class StockValuationResponse {
    @Schema(description = "Menu item ID", example = "1")
    private Long menuItemId;

    @Schema(description = "Menu item name", example = "Margherita Pizza")
    private String menuItemName;

    @Schema(description = "Current stock quantity", example = "10")
    private Integer stockQuantity;

    @Schema(description = "Unit cost", example = "5.99")
    private BigDecimal unitCost;

    @Schema(description = "Total value", example = "59.90")
    private BigDecimal totalValue;

    @Schema(description = "Last valuation date")
    private LocalDateTime lastValuationDate;
} 