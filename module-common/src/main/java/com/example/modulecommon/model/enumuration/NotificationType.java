package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    PRODUCT_QNA_REPLY("PRODUCT_QNA_REPLY", " 상품 문의에 답변이 작성되었습니다."),
    MEMBER_QNA_REPLY("MEMBER_QNA_REPLY", " 문의에 답변이 작성되었습니다."),
    REVIEW_REPLY("REVIEW_REPLY", "작성하신 리뷰에 답변이 작성되었습니다."),
    ORDER_STATUS("ORDER_STATUS", "주문하신 상품이 확인되어 곧 발송 예정입니다.");

    private final String type;

    private final String title;
}
