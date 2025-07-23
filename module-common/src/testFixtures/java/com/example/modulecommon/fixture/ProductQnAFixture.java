package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;

import java.util.ArrayList;
import java.util.List;

public class ProductQnAFixture {

    public static List<ProductQnA> createProductQnACompletedAnswer(List<Member> members, List<Product> products) {
        List<ProductQnA> result = createDefaultProductQnA(members, products);

        result.forEach(v -> v.setProductQnAStat(true));

        return result;
    }

    public static List<ProductQnA> createDefaultProductQnA(List<Member> members, List<Product> products) {
        List<ProductQnA> result = new ArrayList<>();

        for(Member m : members) {
            for(Product p : products) {
                result.add(createProductQnA(m, p));
            }
        }

        return result;
    }

    private static ProductQnA createProductQnA(Member member, Product product) {
        return ProductQnA.builder()
                .member(member)
                .product(product)
                .qnaContent(product.getProductName() + " QNA. Writer " + member.getUserId())
                .build();
    }

    public static List<ProductQnAReply> createDefaultProductQnaReply(Member admin, List<ProductQnA> productQnAs) {
        return productQnAs.stream().map(v -> createProductQnAReply(admin, v)).toList();
    }

    private static ProductQnAReply createProductQnAReply(Member admin, ProductQnA productQnA) {
        return ProductQnAReply.builder()
                .productQnA(productQnA)
                .member(admin)
                .replyContent(productQnA.getQnaContent() + " reply")
                .build();
    }
}
