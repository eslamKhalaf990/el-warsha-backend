package com.warsha.erp.dtos;

import lombok.Getter;

@Getter
public class CustomerCountByGovernorate {
    private final String governorate;
    private final Long count;

    public CustomerCountByGovernorate(String governorate, Long count) {
        this.governorate = governorate;
        this.count = count;
    }
}
