package com.example.moduleproduct.repository.productLike;

import com.example.modulecommon.model.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long>, ProductLikeDSLRepository {
    //ProductWriteUseCase Integration test QueryMethod
    List<ProductLike> findByMember_UserId(String memberUserId);
}
