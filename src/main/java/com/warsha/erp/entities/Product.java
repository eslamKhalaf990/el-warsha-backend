package com.warsha.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Long ProductID;

    @Column(name = "Name")
    private String Name;

    @Column(name = "Description")
    private String Description;

    @Column(name = "BuyingPrice")
    private Double BuyingPrice;

    @Column(name = "SellingPrice")
    private Double SellingPrice;

    @Column(name = "Category")
    private String Category;

    @Column(name = "ImageUrl")
    private String ImageUrl;

    @Column(name = "SKU", insertable = false, updatable = false)
    private String Sku;

    @Column(name = "Quantity")
    private String Quantity;

    @Column(name = "IsDeleted")
    private String deleted;

    @Column(name = "CreatedAt")
    private LocalDateTime CreatedAt;

    @Column(name = "DeletedAt")
    private LocalDateTime DeletedAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime UpdatedAt;
}
