package com.example.moduleproduct.repository.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import org.springframework.data.domain.Page;

public interface ProductLikeDSLRepository {

    int countByUserIdAndProductId(String productId, String userId);

    void deleteByUserIdAndProductId(ProductLike entity);

    Page<?> findByUserId();
}
