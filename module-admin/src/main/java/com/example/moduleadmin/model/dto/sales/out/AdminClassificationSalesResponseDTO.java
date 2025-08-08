package com.example.moduleadmin.model.dto.sales.out;

import com.example.moduleadmin.model.dto.sales.business.AdminClassificationSalesDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminClassificationSalesProductListDTO;

import java.util.List;

public record AdminClassificationSalesResponseDTO(
        String classification,
        long totalSales,
        long totalSalesQuantity,
        List<AdminClassificationSalesProductListDTO> productList
) {

    public AdminClassificationSalesResponseDTO(String classification,
                                            AdminClassificationSalesDTO classificationSalesDTO,
                                            List<AdminClassificationSalesProductListDTO> product) {
        this(
                classification,
                classificationSalesDTO.sales(),
                classificationSalesDTO.salesQuantity(),
                product
        );
    }
}
