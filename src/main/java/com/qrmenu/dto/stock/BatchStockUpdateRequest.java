package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Batch stock update request")
public class BatchStockUpdateRequest {
    @NotEmpty(message = "Stock updates cannot be empty")
    @Valid
    private List<StockUpdate> updates;

    @Data
    @Schema(description = "Individual stock update")
    public static class StockUpdate {
        @NotNull(message = "Menu item ID is required")
        @Schema(description = "Menu item ID", example = "1")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        @Schema(description = "New stock quantity", example = "10")
        private Integer quantity;
    }
} 