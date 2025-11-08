package com.warsha.erp.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDto {

    private Long customerId;
    private String fullName;
    private String governorate;
    private String city;
    private String secondaryPhone;
    private String phone;
    private String address;
}