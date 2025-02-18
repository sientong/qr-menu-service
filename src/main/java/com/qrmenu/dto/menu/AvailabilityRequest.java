package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to update menu item availability")
public class AvailabilityRequest {
    @Schema(description = "Whether the item is available", example = "true")
    private boolean available;
} 