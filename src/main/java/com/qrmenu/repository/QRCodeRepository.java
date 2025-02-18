package com.qrmenu.repository;

import com.qrmenu.model.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {
    Optional<QRCode> findByCodeAndActiveTrue(String code);
    
    List<QRCode> findByRestaurantIdAndActiveTrue(Long restaurantId);
    
    @Query("SELECT q FROM QRCode q WHERE q.expiresAt <= :now AND q.active = true")
    List<QRCode> findExpiredQRCodes(LocalDateTime now);
} 