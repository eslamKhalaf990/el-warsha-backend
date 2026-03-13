package com.warsha.erp.dtos;

// For Daily Revenue Report
public record DailyRevenueReportDto(
    String salesDate, // Returns dd MM yyyy
    Long numberOfOrders, 
    Double totalDiscountsGiven, 
    Double netRevenue
) {}