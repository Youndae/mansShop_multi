package com.example.moduleproduct.repository.product;


import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.admin.product.business.AdminProductStockDataDTO;
import com.example.moduleproduct.model.dto.admin.product.in.AdminDiscountPatchDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminDiscountProductDTO;
import com.example.moduleproduct.model.dto.admin.product.out.AdminProductListDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.AdminProductPageDTO;
import com.example.moduleproduct.model.dto.page.MainPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductIdClassificationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductDSLRepository {

    List<MainListDTO> findListDefault(MainPageDTO pageDTO);

    Page<MainListDTO> findListPageable(MainPageDTO pageDTO, Pageable pageable);

    void patchProductSalesQuantity(Map<String, Integer> productMap);

    List<ProductIdClassificationDTO> findClassificationAllByProductIds(List<String> productIds);

    List<AdminProductListDTO> findAdminProductPageList(AdminProductPageDTO pageDTO);

    Long findAdminProductListCount(AdminProductPageDTO pageDTO);

    List<AdminProductStockDataDTO> findAllProductStockData(AdminProductPageDTO pageDTO);

    Long findAllProductStockDataCount(AdminProductPageDTO pageDTO);

    Page<Product> getDiscountProductList(AdminProductPageDTO pageDTO, Pageable pageable);

    List<AdminDiscountProductDTO> findAllProductByClassificationId(String classificationId);

    void patchProductDiscount(AdminDiscountPatchDTO patchDTO);
}
