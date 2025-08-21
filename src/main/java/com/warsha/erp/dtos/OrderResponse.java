package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private String status;

    private double discount;
    private double delivery;
    private double totalPrice;
    private String orderSource;
    private String paymentMethod;

    private LocalDate orderDate;
    private CustomerDto customer;
    private List<OrderItemDto> orderItems;
}