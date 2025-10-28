package com.warsha.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "BankTransactions")
public class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "BankAccountID", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private TransactionCategory category;

    @Column(name = "TransactionType")
    private String transactionType; // Deposit, Withdrawal

    @Column(name = "Amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "Description")
    private String description;

    @Column(name = "ReferenceType")
    private String referenceType; // e.g., Invoice, Order

    @Column(name = "ReferenceID")
    private Long referenceId;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();
}
