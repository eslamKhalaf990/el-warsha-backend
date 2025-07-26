package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    //each item have product
    private Long productId;

    //how many each product
    private Integer quantity;

    //how much the unit
    private Double unitPrice;
}
