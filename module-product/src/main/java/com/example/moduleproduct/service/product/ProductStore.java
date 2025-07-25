package com.example.moduleproduct.service.product;

import com.example.moduleproduct.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductStore {

    private final ProductRepository productRepository;

    public void patchProductSalesQuantity(Map<String, Integer> productPatchMap) {
        productRepository.patchProductSalesQuantity(productPatchMap);
    }
}
