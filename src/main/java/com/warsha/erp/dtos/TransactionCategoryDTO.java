package com.warsha.erp.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransactionCategoryDTO {
    private Long categoryID;
    private String categoryName;
    private String categoryType;

}