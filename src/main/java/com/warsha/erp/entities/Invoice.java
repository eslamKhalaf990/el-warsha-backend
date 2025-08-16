package com.warsha.erp.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "Invoices")
public class Invoice {
    @Id
    @Column(name="InvoiceID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="InvoiceNumber")
    private String invoiceNumber;

    @Column(name="Notes")
    private String notes;

    @Column(name="InvoiceDate")
    private LocalDate issuedDate = LocalDate.now();

    @OneToOne
    @JoinColumn(name = "OrderID", unique = true)  // ensures 1-to-1
    @JsonManagedReference
    private Order order;
}
