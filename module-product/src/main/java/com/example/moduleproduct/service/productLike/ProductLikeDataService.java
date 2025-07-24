package com.example.moduleproduct.service.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductLikeDataService {

    private final ProductLikeRepository productLikeRepository;

    public boolean getProductLikeStatusByUser(String userId, String productId) {

        return productLikeRepository.countByUserIdAndProductId(productId, userId) == 1;
    }

    public void saveProductLike(ProductLike productLike) {
        productLikeRepository.save(productLike);
    }

    public void deleteProductLike(ProductLike productLike) {
        productLikeRepository.deleteByUserIdAndProductId(productLike);
    }
}
