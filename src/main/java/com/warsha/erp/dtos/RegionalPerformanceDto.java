package com.warsha.erp.dtos;

// For Sales Performance by Governorate
public record RegionalPerformanceDto(
    String governorate, 
    Long totalOrders, 
    Double totalRevenue, 
    Double totalDeliveryCollected
) {}
