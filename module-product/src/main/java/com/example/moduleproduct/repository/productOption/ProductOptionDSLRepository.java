package com.example.moduleproduct.repository.productOption;

import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;

import java.util.List;

public interface ProductOptionDSLRepository {

    List<ProductOptionDTO> findByDetailOption(String productId);

    void patchOrderStock(List<PatchOrderStockDTO> patchDTO);

    List<OrderProductInfoDTO> findOrderData(List<Long> optionIds);
}
