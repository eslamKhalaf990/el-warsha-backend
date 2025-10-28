package com.warsha.erp.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BankAccountDTO {
    private Long id;
    private String name;
    private String accountType;
    private BigDecimal currentBalance;
    private LocalDateTime createdAt;
}
