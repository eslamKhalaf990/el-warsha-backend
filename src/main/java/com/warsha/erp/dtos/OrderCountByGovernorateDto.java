package com.warsha.erp.dtos;

import lombok.Getter;

@Getter
public class OrderCountByGovernorateDto {
    private final String governorate;
    private final Long orderCount;

    public OrderCountByGovernorateDto(String governorate, Long orderCount) {
        this.governorate = governorate;
        this.orderCount = orderCount;
    }
}
