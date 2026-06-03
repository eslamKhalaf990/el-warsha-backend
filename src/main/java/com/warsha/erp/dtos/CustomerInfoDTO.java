package com.warsha.erp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDTO {
    private Long id;
    private String fullName;
    private String governorate;
    private String phone;
    private String city;
    private String secondaryPhone;
    private String address;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
