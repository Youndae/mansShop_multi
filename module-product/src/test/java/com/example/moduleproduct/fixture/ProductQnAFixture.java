package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.modulecommon.model.enumuration.PageAmount;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ProductQnAFixture {

    /**
     * Create ProductQnA Entity
     * 상품 문의 Entity 생성 후 반환
     * 여러개의 문의를 확인할 때 content의 값을 통해 비교하기 위해 qnANum을 파라미터로 받아 추가
     * @param qnANum
     * @return
     */
    public static ProductQnA createProductQnA(int qnANum) {
        Product product = ProductFixture.createProduct(0);
        Member member = MemberFixture.createMember();

        return ProductQnA.builder()
                        .member(member)
                        .product(product)
                        .qnaContent("TestQnAContent" + qnANum)
                        .productQnAStat(false)
                        .build();
    }

    /**
     * Create ProductQnA and ProductQnAReply
     * 상품 문의와 그 답변에 대한 데이터를 담고 있는 ProductQnA Entity 생성 후 반환
     * @param qnANum
     * @return
     */
    public static ProductQnA createProductQnAAndReply(int qnANum) {
        ProductQnA productQnA = createProductQnA(qnANum);

        for(int i = 0; i < 5; i++) {
            ProductQnAReply productQnAReply = createProductQnAReply(i, productQnA);
            productQnA.addProductQnAReply(productQnAReply);
        }

        return productQnA;
    }

    /**
     * Create ProductQnA Pageable Object
     * 상품 문의 조회 시 필요한 Pageable 객체 생성 후 반환.
     * @return
     */
    public static Pageable createQnAPageable() {
        return PageRequest.of(0, PageAmount.PRODUCT_QNA_AMOUNT.getAmount(), Sort.by("id").descending());
    }

    /**
     * Create ProductQnAReply
     * 상품 문의 답변 Entity 생성 후 반환
     *
     * @param i
     * @param productQnA
     * @return
     */
    public static ProductQnAReply createProductQnAReply(int i, ProductQnA productQnA) {
        return ProductQnAReply.builder()
                .member(productQnA.getMember())
                .productQnA(productQnA)
                .replyContent("TestQnAReplyContent" + i)
                .build();
    }
}
