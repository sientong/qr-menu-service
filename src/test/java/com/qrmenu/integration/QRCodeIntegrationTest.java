package com.qrmenu.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.model.QRCode;
import com.qrmenu.model.Restaurant;
import com.qrmenu.model.RestaurantTable;
import com.qrmenu.repository.QRCodeRepository;
import com.qrmenu.repository.RestaurantRepository;
import com.qrmenu.repository.RestaurantTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QRCodeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    private Restaurant testRestaurant;
    private RestaurantTable testTable;

    @BeforeEach
    void setUp() {
        testRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Test Restaurant")
                .active(true)
                .build());

        testTable = tableRepository.save(RestaurantTable.builder()
                .restaurant(testRestaurant)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldGenerateAndValidateQRCode() throws Exception {
        // Generate QR code
        String responseJson = mockMvc.perform(post("/api/v1/restaurants/{restaurantId}/tables/{tableId}/qr-code", 
                testRestaurant.getId(), testTable.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.qrCodeBase64").exists())
                .andExpect(jsonPath("$.menuUrl").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract code from response
        String code = objectMapper.readTree(responseJson).get("code").asText();

        // Validate QR code
        mockMvc.perform(get("/api/v1/qr-codes/{code}/validate", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // Verify QR code in database
        QRCode savedCode = qrCodeRepository.findByCodeAndActiveTrue(code).orElseThrow();
        assertThat(savedCode.getRestaurant().getId()).isEqualTo(testRestaurant.getId());
        assertThat(savedCode.getTable().getId()).isEqualTo(testTable.getId());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_ADMIN")
    void shouldInvalidateQRCode() throws Exception {
        // Create QR code
        QRCode qrCode = qrCodeRepository.save(QRCode.builder()
                .restaurant(testRestaurant)
                .table(testTable)
                .code("test-code")
                .active(true)
                .build());

        // Invalidate QR code
        mockMvc.perform(delete("/api/v1/qr-codes/{code}", qrCode.getCode()))
                .andExpect(status().isNoContent());

        // Verify QR code is inactive
        assertThat(qrCodeRepository.findByCodeAndActiveTrue(qrCode.getCode()))
                .isEmpty();
    }

    @Test
    void shouldRequireAuthenticationForQRCodeOperations() throws Exception {
        mockMvc.perform(post("/api/v1/restaurants/1/tables/1/qr-code"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/qr-codes/test-code"))
                .andExpect(status().isUnauthorized());
    }
} 