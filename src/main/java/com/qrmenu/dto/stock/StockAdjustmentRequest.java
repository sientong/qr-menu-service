package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Stock adjustment request")
public class StockAdjustmentRequest {
    @Schema(description = "Quantity to adjust (positive for addition, negative for deduction)", 
            example = "5")
    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @Schema(description = "Reason for the stock adjustment", example = "Restocked from supplier")
    private String reason;

    @Schema(description = "Adjustment type", example = "INVENTORY_COUNT")
    private String type;

    @Schema(description = "Adjustment date", example = "2024-01-01")
    private LocalDateTime date;

    @Schema(description = "Adjustment source", example = "SUPPLIER")
    private String source;

    @Schema(description = "Adjustment method", example = "MANUAL")
    private String method;

    @Schema(description = "Adjustment user", example = "John Doe")
    private String user;

    @Schema(description = "Adjustment notes", example = "Stock adjustment notes")
    private String notes;

    @Schema(description = "Adjustment category", example = "INVENTORY")
    private String category;

    @Schema(description = "Adjustment sub-category", example = "SUPPLIER")
    private String subCategory;
    
} 