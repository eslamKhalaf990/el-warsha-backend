package com.warsha.erp.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private String status;
    private LocalDate orderDate;
    private CustomerDto customer;
    private List<OrderItemDto> orderItems;
}