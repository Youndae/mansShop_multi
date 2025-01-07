package com.example.moduleproduct.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ProductQnAFixture {

    /**
     * Create ProductQnA Entity
     * 상품 문의 Entity 생성 후 반환
     * 여러개의 문의를 확인할 때 content의 값을 통해 비교하기 위해 qnANum을 파라미터로 받아 추가
     * @param qnANum
     * @return
     */
    public static ProductQnA createProductQnA(int qnANum) {
        Product product = ProductFixture.createOneProductEntity();
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
        return PageRequest.of(0, PageAmount.PRODUCT_QNA_AMOUNT.getAmount(), Sort.by("createdAt").descending());
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

    public static Page<ProductQnADTO> createPageObjectByProductQnA() {
        List<ProductQnADTO> productQnADTOS = IntStream.range(0, 5)
                                            .mapToObj(v -> createProductQnADTO(v, v % 2 == 0))
                                            .toList();

        return new PageImpl<>(productQnADTOS, createQnAPageable(), productQnADTOS.size());
    }

    public static ProductQnADTO createProductQnADTO(int i, boolean status) {
        return new ProductQnADTO((long) i,
                                "writer" + i,
                                "content" + i,
                                LocalDate.now(),
                                status
                        );
    }

    public static List<ProductQnAReply> createProductQnAReplyList() {
        //TODO: 0, 2, 4 만 reply가 존재해야 한다.
        List<ProductQnA> qnaEntities = new ArrayList<>();

        for(int i = 0; i < 5; i += 2)
            qnaEntities.add(ProductQnA.builder()
                                    .id((long) i)
                                    .build()
                            );

        List<ProductQnAReply> replyList = new ArrayList<>();
        Member member = MemberFixture.createMember();

        for(int i = 0; i < qnaEntities.size(); i++) {
            ProductQnA qna = qnaEntities.get(i);

            for(int j = 0; j < 3; j++) {
                replyList.add(ProductQnAReply.builder()
                                .member(member)
                                .productQnA(qna)
                                .replyContent("replyContent" + i + " : " + j)
                                .build());
            }
        }

        return replyList;
    }
}
