package com.example.moduleproduct.repository.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long>, ProductLikeDSLRepository {
}
