package com.example.moduleproduct.usecase.admin.productQnA;

import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleproduct.service.productQnA.ProductQnADomainService;
import com.example.moduleproduct.service.productQnA.ProductQnAExternalService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductQnAWriteUseCase {

    private final ProductQnADataService productQnADataService;

    private final ProductQnADomainService productQnADomainService;

    private final ProductQnAExternalService productQnAExternalService;

    private final UserDataService userDataService;

    public void patchProductQnAComplete(long qnaId) {
        ProductQnA entity = productQnADataService.findProductQnAByIdOrElseIllegal(qnaId);
        patchProductQnAStatusAndSave(entity);
    }

    private void patchProductQnAStatusAndSave(ProductQnA productQnA) {
        productQnA.setProductQnAStat(true);
        productQnADataService.saveProductQnA(productQnA);
    }

    public void postProductQnAReply(QnAReplyInsertDTO insertDTO, String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);
        ProductQnA productQnA = productQnADataService.findProductQnAByIdOrElseIllegal(insertDTO.qnaId());
        ProductQnAReply productQnAReply = productQnADomainService.buildProductQnAReply(member, productQnA, insertDTO.content());

        productQnADataService.saveProductQnAReply(productQnAReply);
        patchProductQnAStatusAndSave(productQnA);

        productQnAExternalService.sendProductQnANotification(productQnA);
    }

    public void patchProductQnAReply(QnAReplyPatchDTO replyDTO, String userId) {
        ProductQnAReply productQnAReply = productQnADataService.getProductQnAReplyByIdOrElseIllegal(replyDTO.replyId());

        if(!productQnAReply.getMember().getUserId().equals(userId))
            throw new IllegalArgumentException();

        productQnAReply.setReplyContent(replyDTO.content());
        productQnADataService.saveProductQnAReply(productQnAReply);
    }
}
