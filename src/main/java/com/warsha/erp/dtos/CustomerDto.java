package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDto {

    private Long customerId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
}