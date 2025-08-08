package com.example.moduleproduct.repository.productThumbnail;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductThumbnail.productThumbnail;


@Repository
@RequiredArgsConstructor
public class ProductThumbnailDSLRepositoryImpl implements ProductThumbnailDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<String> findByProductId(String productId) {
        return jpaQueryFactory
                .select(productThumbnail.imageName)
                .from(productThumbnail)
                .where(productThumbnail.product.id.eq(productId))
                .orderBy(productThumbnail.imageName.asc())
                .fetch();
    }

    @Override
    @Transactional
    public void deleteAllByImageNames(List<String> imageNames) {
        jpaQueryFactory.delete(productThumbnail)
                .where(productThumbnail.imageName.in(imageNames))
                .execute();
    }
}
