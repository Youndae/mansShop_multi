package com.example.moduleproduct.service.product;

import com.example.modulecommon.model.entity.Product;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDataService {

    private final ProductRepository productRepository;

    private final ProductOptionRepository productOptionRepository;

    private final ProductThumbnailRepository productThumbnailRepository;

    private final ProductInfoImageRepository productInfoImageRepository;

    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<ProductOptionDTO> getProductOptionDTOListByProductId(String productId) {
        return productOptionRepository.findByDetailOption(productId);
    }

    public List<String> getProductThumbnailImageNameList(String productId) {
        return productThumbnailRepository.findByProductId(productId);
    }

    public List<String> getProductInfoImageNameList(String productId) {
        return productInfoImageRepository.findByProductId(productId);
    }
}
