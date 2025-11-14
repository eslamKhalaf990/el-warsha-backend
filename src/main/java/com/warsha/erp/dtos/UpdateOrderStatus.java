package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatus {
    private String status;
    private Long bankAccountId;
}
