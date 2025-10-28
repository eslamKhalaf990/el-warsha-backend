package com.warsha.erp.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "TransactionCategories")
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryID")
    private Long categoryID;

    @Column(name = "CategoryName")
    private String categoryName; // Marketing, Products, Sales Income

    @Column(name = "CategoryType")
    private String categoryType; // Expense, Income
}
