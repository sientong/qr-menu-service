package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "QR code creation request")
public class QRCodeRequest {
    @Schema(description = "QR code name for identification", example = "Front Section QR")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Table number", example = "T12")
    @Size(max = 50, message = "Table number must not exceed 50 characters")
    private String tableNumber;
} 