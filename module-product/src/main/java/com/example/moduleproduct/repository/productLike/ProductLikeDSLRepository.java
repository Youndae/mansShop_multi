package com.example.moduleproduct.repository.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductLikeDSLRepository {

    int countByUserIdAndProductId(String productId, String userId);

    void deleteByUserIdAndProductId(ProductLike entity);

    Page<ProductLikeDTO> findListByUserId(String userId, Pageable pageable);
}
