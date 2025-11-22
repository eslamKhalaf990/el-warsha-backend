package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopProductDTO {

    private Long productId;
    private String name;
    private Integer totalSold;
}