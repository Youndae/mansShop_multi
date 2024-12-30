package com.example.moduleproduct.repository.productOption;

import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;

import java.util.List;

public interface ProductOptionDSLRepository {

    List<ProductOptionDTO> findByDetailOption(String productId);
}
