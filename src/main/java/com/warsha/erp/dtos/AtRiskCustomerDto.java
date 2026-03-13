package com.warsha.erp.dtos;

// For At-Risk / Lapsed Customers
public record AtRiskCustomerDto(
    String fullName, 
    String phone, 
    Long lifetimeOrders, 
    java.util.Date lastOrderDate, 
    Integer daysSinceLastOrder
) {}
