package com.warsha.erp.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidateVoucherRequest {
    private String code;
    private BigDecimal cartTotal;
    // Optional: List<Long> productIds; // If you plan to do product-specific vouchers later
}