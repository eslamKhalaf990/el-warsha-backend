package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CreatePaymentRequest {
    private Long orderId;
    private Double amountPaid;
    private String paymentMethod;
    private String paymentStatus;

}
