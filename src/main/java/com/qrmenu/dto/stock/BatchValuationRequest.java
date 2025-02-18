package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Batch valuation update request")
public class BatchValuationRequest {
    @NotEmpty(message = "Valuations cannot be empty")
    @Valid
    private List<ValuationUpdate> updates;

    @Data
    @Schema(description = "Individual valuation update")
    public static class ValuationUpdate {
        @NotNull(message = "Menu item ID is required")
        @Schema(description = "Menu item ID", example = "1")
        private Long menuItemId;

        @NotNull(message = "Unit cost is required")
        @Positive(message = "Unit cost must be positive")
        @Schema(description = "New unit cost", example = "5.99")
        private BigDecimal unitCost;
    }
} 