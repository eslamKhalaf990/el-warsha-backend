package com.warsha.erp.dtos;

// For Revenue by Order Source (App, Web, etc.)
public record OrderSourceDto(
        String orderSource,
        Long totalOrders,
        Double totalRevenue,
        Double averageOrderValue
) {}
