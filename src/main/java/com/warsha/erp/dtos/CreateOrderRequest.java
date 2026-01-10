package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    private Long customerId;
    private Long bankAccountId;

    private List<OrderItemRequest> items;

    private String paymentMethod;
    private String orderSource;
    private String notes;

    private double downPayment;
    private double totalPrice;
    private double delivery;
    private double discount;
    private String promoCode;
    private List<MultipartFile> images;

    public double getItemsTotalPrice() {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        return items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    public double calculateTotalPriceERP() {
        double itemsTotal = getItemsTotalPrice();
        this.totalPrice = itemsTotal + delivery - discount - downPayment;
        return this.totalPrice;
    }

    public double calculateTotalPriceEcommerce() {
        double itemsTotal = getItemsTotalPrice();
        this.totalPrice = itemsTotal - discount - downPayment;
        return this.totalPrice;
    }
}
