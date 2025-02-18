package com.qrmenu.dto.stock;

import com.qrmenu.model.StockAdjustmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Stock history response")
public class StockHistoryResponse {
    @Schema(description = "History record ID")
    private Long id;

    @Schema(description = "Menu item ID")
    private Long menuItemId;

    @Schema(description = "Menu item name")
    private String menuItemName;

    @Schema(description = "Previous stock quantity")
    private Integer previousQuantity;

    @Schema(description = "New stock quantity")
    private Integer newQuantity;

    @Schema(description = "Adjustment quantity")
    private Integer adjustmentQuantity;

    @Schema(description = "Type of adjustment")
    private StockAdjustmentType adjustmentType;

    @Schema(description = "User who made the adjustment")
    private String adjustedBy;

    @Schema(description = "Adjustment timestamp")
    private LocalDateTime adjustedAt;

    @Schema(description = "Additional notes")
    private String notes;
} 