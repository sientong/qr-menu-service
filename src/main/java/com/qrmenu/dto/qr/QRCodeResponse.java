package com.qrmenu.dto.qr;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QRCodeResponse {
    private String code;
    private String qrCodeBase64;
    private LocalDateTime expirationDate;
    private String menuUrl;
} 