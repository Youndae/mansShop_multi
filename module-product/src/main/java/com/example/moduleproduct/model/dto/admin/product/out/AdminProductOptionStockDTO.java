package com.example.moduleproduct.model.dto.admin.product.out;

import com.example.moduleproduct.model.dto.admin.product.business.AdminOptionStockDTO;

public record AdminProductOptionStockDTO(
        String size,
        String color,
        int optionStock,
        boolean optionIsOpen
) {

    public AdminProductOptionStockDTO(AdminOptionStockDTO productOption) {
        this(
                productOption.size(),
                productOption.color(),
                productOption.optionStock(),
                productOption.optionIsOpen()
        );
    }
}
