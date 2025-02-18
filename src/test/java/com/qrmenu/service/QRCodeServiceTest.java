package com.qrmenu.service;

import com.qrmenu.dto.qr.QRCodeResponse;
import com.qrmenu.model.QRCode;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.RestaurantTable;
import com.qrmenu.repository.QRCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @Mock
    private QRCodeRepository qrCodeRepository;

    @InjectMocks
    private QRCodeService qrCodeService;

    @Test
    void shouldGenerateQRCode() {
        ReflectionTestUtils.setField(qrCodeService, "frontendUrl", "http://localhost:3000");
        
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .build();

        RestaurantTable table = RestaurantTable.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("T1")
                .build();

        when(qrCodeRepository.save(any(QRCode.class)))
                .thenAnswer(i -> i.getArgument(0));

        QRCodeResponse response = qrCodeService.generateQRCode(restaurant, table);

        assertThat(response.getCode()).isNotNull();
        assertThat(response.getQrCodeBase64()).isNotNull();
        assertThat(response.getMenuUrl()).startsWith("http://localhost:3000/menu/");
        verify(qrCodeRepository).save(any(QRCode.class));
    }

    @Test
    void shouldValidateQRCode() {
        String code = "test-code";
        when(qrCodeRepository.findByCodeAndActiveTrue(code))
                .thenReturn(Optional.of(QRCode.builder().build()));

        boolean isValid = qrCodeService.validateQRCode(code);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateQRCode() {
        String code = "test-code";
        QRCode qrCode = QRCode.builder()
                .code(code)
                .active(true)
                .build();

        when(qrCodeRepository.findByCodeAndActiveTrue(code))
                .thenReturn(Optional.of(qrCode));

        qrCodeService.invalidateQRCode(code);

        verify(qrCodeRepository).save(argThat(savedCode -> 
            !savedCode.isActive()
        ));
    }

    @Test
    void shouldCleanupExpiredQRCodes() {
        QRCode expiredCode1 = QRCode.builder()
                .code("expired-1")
                .active(true)
                .build();

        QRCode expiredCode2 = QRCode.builder()
                .code("expired-2")
                .active(true)
                .build();

        when(qrCodeRepository.findExpiredQRCodes(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(expiredCode1, expiredCode2));

        qrCodeService.cleanupExpiredQRCodes();

        verify(qrCodeRepository).saveAll(argThat(codes -> 
            ((Iterable<QRCode>) codes).iterator().next().isActive() == false
        ));
    }
} 