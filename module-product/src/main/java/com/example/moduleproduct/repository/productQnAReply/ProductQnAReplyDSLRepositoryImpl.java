package com.example.moduleproduct.repository.productQnAReply;

import com.example.moduleproduct.model.dto.product.out.ProductDetailQnAReplyListDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
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
    public List<ProductDetailQnAReplyListDTO> getQnAReplyListByQnAIds(List<Long> qnaIds) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                ProductDetailQnAReplyListDTO.class,
                                new CaseBuilder()
                                        .when(productQnAReply.member.nickname.isNull())
                                        .then(productQnAReply.member.userName)
                                        .otherwise(productQnAReply.member.nickname)
                                        .as("writer"),
                                productQnAReply.replyContent.as("replyContent"),
                                productQnAReply.productQnA.id.as("qnaId"),
                                productQnAReply.createdAt
                        )
                )
                .from(productQnAReply)
                .where(productQnAReply.productQnA.id.in(qnaIds))
                .orderBy(productQnAReply.productQnA.id.desc())
                .orderBy(productQnAReply.productQnA.createdAt.asc())
                .fetch();
    }
}
