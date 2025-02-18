package com.qrmenu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_history")
public class StockHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Column(name = "adjustment_quantity", nullable = false)
    private Integer adjustmentQuantity;

    @Column(name = "adjustment_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StockAdjustmentType adjustmentType;

    @Column(name = "adjusted_by", nullable = false)
    private String adjustedBy;

    @Column(name = "adjusted_at", nullable = false)
    private LocalDateTime adjustedAt;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        adjustedAt = LocalDateTime.now();
    }
} 