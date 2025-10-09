package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DailyCashFlowDto {

    private LocalDate day;
    private Long totalOrders;
    private BigDecimal dailyCashReceived;
    private BigDecimal dailyShippedValue;
    private BigDecimal dailyDeliveryCharges;
}
