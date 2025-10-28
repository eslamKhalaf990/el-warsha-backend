package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BankTransactionDTO {
    private Long bankAccountId;
    private Long categoryId;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private String referenceType;
    private Long referenceId;
}
