package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    private Long customerId;

    private List<OrderItemRequest> items;

    private String paymentMethod;
    private String orderSource;

    private double downPayment;
    private double totalPrice;
    private double delivery;
    private double discount;

    public double getItemsTotalPrice() {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        return items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    public double calculateTotalPrice() {
        double itemsTotal = getItemsTotalPrice();
        this.totalPrice = itemsTotal + delivery - discount - downPayment;
        return this.totalPrice;
    }
}
