package com.warsha.erp.dtos;

// For Discount Seekers
public record DiscountSeekerDto(
        String fullName,
        Long totalOrders,
        Double totalPaid,
        Double totalDiscountsReceived
) {}
