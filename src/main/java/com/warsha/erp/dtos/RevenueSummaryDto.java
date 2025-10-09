package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RevenueSummaryDto {
    private BigDecimal actualCashReceived;
    private BigDecimal expectedCash;
    private BigDecimal potentialRevenue;

}
