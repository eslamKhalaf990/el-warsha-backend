package com.warsha.erp.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Products")
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Long ProductID;

    @Column(name = "Name")
    private String Name;

    @Column(name = "Description")
    private String Description;

    @Column(name = "Price")
    private Double Price;

    @Column(name = "Category")
    private String Category;

    @Column(name = "Sku")
    private String Sku;

    @Column(name = "CreatedAt")
    private LocalDateTime CreatedAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime UpdatedAt;
}
