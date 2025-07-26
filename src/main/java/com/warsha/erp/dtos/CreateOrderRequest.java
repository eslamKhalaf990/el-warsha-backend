package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CreateOrderRequest {
    //foreign key to customer
    private Long customerId;

    //have multiple items
    private List<OrderItemRequest> items;
}
