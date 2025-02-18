package com.qrmenu.dto.stock;

import com.qrmenu.model.StockAlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Stock alert response")
public class StockAlertResponse {
    @Schema(description = "Alert ID", example = "1")
    private Long id;

    @Schema(description = "Menu item ID", example = "1")
    private Long menuItemId;

    @Schema(description = "Menu item name", example = "Margherita Pizza")
    private String menuItemName;

    @Schema(description = "Alert type", example = "LOW_STOCK")
    private StockAlertType alertType;

    @Schema(description = "Alert message")
    private String message;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Acknowledgment timestamp")
    private LocalDateTime acknowledgedAt;

    @Schema(description = "Acknowledged by")
    private String acknowledgedBy;
} 