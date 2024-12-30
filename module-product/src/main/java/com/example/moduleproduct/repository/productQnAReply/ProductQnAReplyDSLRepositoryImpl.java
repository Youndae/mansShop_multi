package com.example.moduleproduct.repository.productQnAReply;

import com.example.modulecommon.model.entity.ProductQnAReply;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.modulecommon.model.entity.QProductQnAReply.productQnAReply;

@Repository
@RequiredArgsConstructor
public class ProductQnAReplyDSLRepositoryImpl implements ProductQnAReplyDSLRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProductQnAReply> findByQnAReply(List<Long> qnaIds) {
        return jpaQueryFactory.select(productQnAReply)
                .from(productQnAReply)
                .where(productQnAReply.productQnA.id.in(qnaIds))
                .orderBy(productQnAReply.productQnA.id.desc())
                .orderBy(productQnAReply.productQnA.createdAt.asc())
                .fetch();
    }
}
