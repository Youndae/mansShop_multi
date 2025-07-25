package com.example.moduleproduct.repository.product;


import com.example.moduleproduct.model.dto.main.business.MainListDTO;
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
}
