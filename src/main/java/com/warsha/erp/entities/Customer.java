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
    private Long id;

    @Column(name = "FullName")
    private String fullName;

    @Column(name = "Governorate")
    private String governorate;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "City")
    private String city;

    @Column(name = "SecondaryPhone")
    private String secondaryPhone;

    @Column(name = "Address")
    private String address;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
