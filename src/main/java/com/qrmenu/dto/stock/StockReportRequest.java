package com.qrmenu.dto.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Stock report request")
public class StockReportRequest {
    @NotNull(message = "Start date is required")
    @Schema(description = "Report start date")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "Report end date")
    private LocalDateTime endDate;

    @Schema(description = "Menu item ID (optional)", example = "1")
    private Long menuItemId;
} 