package com.warsha.erp.dtos;

import com.warsha.erp.entities.Payment;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDto {
    private Long id;
    private Double amountPaid;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private Long orderId;

    public PaymentDto(Payment payment) {
        this.id = payment.getId();
        this.amountPaid = payment.getAmountPaid();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentStatus = payment.getPaymentStatus();
        this.paymentDate = payment.getPaymentDate();
        this.orderId = payment.getOrder().getId();
    }
}
