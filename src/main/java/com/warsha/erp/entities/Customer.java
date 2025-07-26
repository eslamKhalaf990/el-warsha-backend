package com.warsha.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Long CustomerID;

    @Column(name = "FullName")
    private String FullName;

    @Column(name = "Email")
    private String Email;

    @Column(name = "Phone")
    private String Phone;

    @Column(name = "Address")
    private String Address;

    @Column(name = "CreatedAt")
    private LocalDateTime CreatedAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime UpdatedAt;
}
