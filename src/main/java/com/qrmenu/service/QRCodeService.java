package com.qrmenu.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qrmenu.dto.qr.QRCodeResponse;
import com.qrmenu.model.QRCode;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.RestaurantTable;
import com.qrmenu.repository.QRCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing QR codes associated with restaurant tables.
 * Provides functionality for generating, validating, and managing QR codes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QRCodeService {
    private final QRCodeRepository qrCodeRepository;
    
    @Value("${application.qrcode.expiration-days:30}")
    private int qrCodeExpirationDays;
    
    @Value("${application.frontend.url}")
    private String frontendUrl;

    /**
     * Generates a new QR code for a restaurant table.
     *
     * @param restaurant Restaurant for which to generate the QR code
     * @param table Table for which to generate the QR code
     * @return QR code response containing the code, base64 image, and menu URL
     */
    @Transactional
    public QRCodeResponse generateQRCode(Restaurant restaurant, RestaurantTable table) {
        String uniqueCode = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(qrCodeExpirationDays);

        QRCode qrCode = QRCode.builder()
                .code(uniqueCode)
                .restaurant(restaurant)
                .table(table)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        qrCodeRepository.save(qrCode);

        String menuUrl = String.format("%s/menu/%s", frontendUrl, uniqueCode);
        byte[] qrCodeImage = generateQRCodeImage(menuUrl);

        return QRCodeResponse.builder()
                .code(uniqueCode)
                .qrCodeBase64(Base64.getEncoder().encodeToString(qrCodeImage))
                .expirationDate(expiresAt)
                .menuUrl(menuUrl)
                .build();
    }

    /**
     * Validates a QR code.
     *
     * @param code The QR code to validate
     * @return true if the code is valid and active, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateQRCode(String code) {
        return qrCodeRepository.findByCodeAndActiveTrue(code).isPresent();
    }

    /**
     * Invalidates a QR code by marking it as inactive.
     *
     * @param code The QR code to invalidate
     */
    @Transactional
    public void invalidateQRCode(String code) {
        qrCodeRepository.findByCodeAndActiveTrue(code)
                .ifPresent(qrCode -> {
                    qrCode.setActive(false);
                    qrCodeRepository.save(qrCode);
                });
    }

    /**
     * Scheduled task to cleanup expired QR codes by marking them as inactive.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredQRCodes() {
        List<QRCode> expiredCodes = qrCodeRepository.findExpiredQRCodes(LocalDateTime.now());
        expiredCodes.forEach(code -> code.setActive(false));
        qrCodeRepository.saveAll(expiredCodes);
    }

    private byte[] generateQRCodeImage(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }
} 