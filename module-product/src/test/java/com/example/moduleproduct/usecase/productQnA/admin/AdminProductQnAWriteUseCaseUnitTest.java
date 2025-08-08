package com.example.moduleproduct.usecase.productQnA.admin;

import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleproduct.service.productQnA.ProductQnAExternalService;
import com.example.moduleproduct.usecase.admin.productQnA.AdminProductQnAWriteUseCase;
import com.example.moduleuser.service.UserDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductQnAWriteUseCaseUnitTest {

    @InjectMocks
    private AdminProductQnAWriteUseCase adminProductQnAWriteUseCase;

    @Mock
    private ProductQnADataService productQnADataService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private ProductQnAExternalService productQnAExternalService;

    @Test
    @DisplayName(value = "상품 문의 답변 완료 상태로 갱신. 문의 아이디가 잘못된 경우")
    void patchProductQnACompleteWrongId() {

        when(productQnADataService.findProductQnAByIdOrElseIllegal(anyLong())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductQnAWriteUseCase.patchProductQnAComplete(0L)
        );

        verify(productQnADataService, never()).saveProductQnA(any(ProductQnA.class));
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 작성자 아이디가 잘못된 경우")
    void postProductQnaReplyWrongUserId() {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, "testContent");
        when(userDataService.getMemberByUserIdOrElseIllegal(any())).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> adminProductQnAWriteUseCase.postProductQnAReply(insertDTO, "tester"));

        verify(productQnADataService, never()).findProductQnAByIdOrElseIllegal(anyLong());
        verify(productQnADataService, never()).saveProductQnAReply(any(ProductQnAReply.class));
        verify(productQnADataService, never()).saveProductQnA(any(ProductQnA.class));
        verify(productQnAExternalService, never()).sendProductQnANotification(any(ProductQnA.class));
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 작성자 아이디가 잘못된 경우")
    void postProductQnaReplyWrongQnAId() {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, "testContent");
        Member member = Member.builder().userId("admin").build();
        when(userDataService.getMemberByUserIdOrElseIllegal(any())).thenReturn(member);
        when(productQnADataService.findProductQnAByIdOrElseIllegal(anyLong())).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> adminProductQnAWriteUseCase.postProductQnAReply(insertDTO, member.getUserId()));

        verify(productQnADataService, never()).saveProductQnAReply(any(ProductQnAReply.class));
        verify(productQnADataService, never()).saveProductQnA(any(ProductQnA.class));
        verify(productQnAExternalService, never()).sendProductQnANotification(any(ProductQnA.class));
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 아이디가 잘못된 경우")
    void patchProductQnAReplyWrongReplyId() {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(1L, "testContent");

        when(productQnADataService.getProductQnAReplyByIdOrElseIllegal(anyLong())).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> adminProductQnAWriteUseCase.patchProductQnAReply(replyDTO, "tester"));

        verify(productQnADataService, never()).saveProductQnAReply(any(ProductQnAReply.class));
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 작성자와 요청자가 다른 경우")
    void patchProductQnAReplyNotEqualsWriter() {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(1L, "testContent");
        ProductQnAReply productQnAReply = ProductQnAReply.builder().id(1L).member(Member.builder().userId("admin").build()).build();
        when(productQnADataService.getProductQnAReplyByIdOrElseIllegal(anyLong())).thenReturn(productQnAReply);

        assertThrows(IllegalArgumentException.class, () -> adminProductQnAWriteUseCase.patchProductQnAReply(replyDTO, "tester"));

        verify(productQnADataService, never()).saveProductQnAReply(any(ProductQnAReply.class));
    }


}
