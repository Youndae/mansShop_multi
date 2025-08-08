package com.example.moduleproduct.repository.productOption;

import com.example.moduleproduct.model.dto.admin.product.business.AdminOptionStockDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;

import java.util.List;

public interface ProductOptionDSLRepository {

    List<ProductOptionDTO> findByDetailOption(String productId);

    void patchOrderStock(List<PatchOrderStockDTO> patchDTO);

    List<OrderProductInfoDTO> findOrderData(List<Long> optionIds);

    List<AdminProductOptionDTO> findAllAdminOptionDTOByProductId(String productId);

    List<AdminOptionStockDTO> findAllProductOptionStockByProductIds(List<String> productIds);
}
