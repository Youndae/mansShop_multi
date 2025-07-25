package com.example.moduleproduct.model.dto.product.business;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchOrderStockDTO {
    private Long optionId;
    private int productCount;
}
