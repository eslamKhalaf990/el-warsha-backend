package com.warsha.erp.entities;

import jakarta.persistence.*; // Use javax.persistence if on older Spring Boot
import lombok.Data; // Assuming you use Lombok for Getters/Setters
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "PromoCodes") // Links to the table we designed
@Data
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromoID")
    private Long id;

    @Column(unique = true, nullable = false, name = "Code")
    private String code; // e.g., "SAVE10"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "DiscountType")
    private DiscountType type; // PERCENTAGE or FIXED

    @Column(nullable = false, name = "DiscountValue")
    private BigDecimal discountValue; // e.g., 10.00


    @Column(name = "MinOrderAmount")
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "MaxDiscountAmount")
    private BigDecimal maxDiscountAmount; // Nullable (No cap if null)

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate; // Nullable (No expiry if null)

    @Column(name = "IsActive")
    private boolean active = true;

    @Column(name = "MaxUsageLimit")
    private Integer maxUsageLimit; // Nullable (Unlimited if null)

    @Column(name = "CurrentUsageCount")
    private Integer currentUsageCount = 0;
}