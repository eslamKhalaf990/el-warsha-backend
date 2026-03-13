package com.warsha.erp.dtos;

// For VIP Customers (Highest LTV)
public record VipCustomerDto(
    String fullName, 
    Long totalOrders, 
    Double totalLifetimeSpend, 
    Double averageOrderValue
) {}
