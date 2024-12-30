package com.example.moduleproduct.repository.product;

import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.page.ProductPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductDSLRepository {

    List<MainListDTO> getProductDefaultList(ProductPageDTO pageDTO);

    Page<MainListDTO> getProductClassificationAndSearchList(ProductPageDTO pageDTO, Pageable pageable);
}
