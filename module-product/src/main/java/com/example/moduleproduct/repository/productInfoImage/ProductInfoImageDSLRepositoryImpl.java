package com.example.moduleproduct.repository.productInfoImage;


import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductInfoImage.productInfoImage;

@Repository
@RequiredArgsConstructor
public class ProductInfoImageDSLRepositoryImpl implements ProductInfoImageDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<String> findByProductId(String productId) {
        return jpaQueryFactory
                .select(productInfoImage.imageName)
                .from(productInfoImage)
                .where(productInfoImage.product.id.eq(productId))
                .orderBy(productInfoImage.imageName.asc())
                .fetch();
    }
}
