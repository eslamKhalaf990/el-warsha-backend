package com.warsha.erp.dtos;

// For Repeat Customers
public record CustomerLoyaltyDto(Long customerId, String fullName, Long orderCount) {}

