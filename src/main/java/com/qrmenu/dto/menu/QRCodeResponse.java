package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "QR code response")
public class QRCodeResponse {
    @Schema(description = "QR code ID", example = "1")
    private Long id;

    @Schema(description = "Restaurant ID", example = "1")
    private Long restaurantId;

    @Schema(description = "Unique code", example = "abc123")
    private String code;

    @Schema(description = "QR code name", example = "Front Section QR")
    private String name;

    @Schema(description = "Table number", example = "T12")
    private String tableNumber;

    @Schema(description = "QR code image data URL")
    private String qrCodeImage;

    @Schema(description = "Expiration timestamp")
    private LocalDateTime expiresAt;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
} 