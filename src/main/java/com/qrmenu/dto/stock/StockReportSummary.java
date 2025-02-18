package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Stock report summary")
public class StockReportSummary {
    @Schema(description = "Total items", example = "10")
    private int totalItems;

    @Schema(description = "Total stock value", example = "1250.50")
    private BigDecimal totalStockValue;

    @Schema(description = "Low stock items count", example = "3")
    private int lowStockItemsCount;

    @Schema(description = "Out of stock items count", example = "1")
    private int outOfStockItemsCount;

    @Schema(description = "Average stock level", example = "85.5")
    private double averageStockLevel;

    @Schema(description = "Most adjusted item")
    private String mostAdjustedItem;

    @Schema(description = "Most valuable item")
    private String mostValuableItem;
} 