package com.example.moduleproduct.service.product;

import com.example.moduleproduct.model.dto.product.business.ProductIdClassificationDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductReader {

    private final ProductRepository productRepository;

    public List<ProductIdClassificationDTO> findClassificationIdAndProductIdByProductIds(List<String> productIds) {
        return productRepository.findClassificationAllByProductIds(productIds);
    }
}
