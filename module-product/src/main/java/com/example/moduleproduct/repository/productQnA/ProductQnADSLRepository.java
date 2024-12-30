package com.example.moduleproduct.repository.productQnA;

import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQnADSLRepository {

    Page<ProductQnADTO> findByProductId(String productId, Pageable pageable);
}
