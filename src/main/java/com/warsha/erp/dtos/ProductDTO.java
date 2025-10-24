package com.warsha.erp.dtos;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String deleted;
    private LocalDateTime deletedAt;
    private String description;
    private Double buyingPrice;
    private Double sellingPrice;
    private String imageUrl;
    private String sku;
    private String quantity;
    private String categoryName;
    private Long categoryId;
}
