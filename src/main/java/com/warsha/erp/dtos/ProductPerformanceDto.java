package com.warsha.erp.dtos;

// For Best-Selling Products (Top 20)
public record ProductPerformanceDto(
    Long productId, 
    Long totalUnitsSold, 
    Double totalRevenueGenerated
) {}

