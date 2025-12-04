package com.example.modulemypage.usecase.admin;

import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.QnAClassification;
import com.example.modulemypage.service.MemberQnADataService;
import com.example.modulemypage.service.MemberQnAExternalService;
import com.example.modulemypage.usecase.MemberQnAWriteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberQnAWriteUseCase {

    private final MemberQnADataService memberQnADataService;

    private final MemberQnAWriteUseCase memberQnAWriteUseCase;

    private final MemberQnAExternalService memberQnAExternalService;

    public void patchMemberQnAComplete(long qnaId) {
        MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(qnaId);
        patchMemberQnAStatusAndSave(memberQnA);
    }

    private void patchMemberQnAStatusAndSave(MemberQnA memberQnA) {
        memberQnA.setMemberQnAStat(true);
        memberQnADataService.saveMemberQnA(memberQnA);
    }

    public void postMemberQnAReply(QnAReplyInsertDTO insertDTO, String userId) {
        memberQnAWriteUseCase.postMemberQnAReply(insertDTO, userId);

        MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(insertDTO.qnaId());
        patchMemberQnAStatusAndSave(memberQnA);

        memberQnAExternalService.sendMemberQnANotification(insertDTO.qnaId(), memberQnA);
    }

    public void postQnAClassification(String classification) {
        QnAClassification entity = QnAClassification.builder()
                                    .qnaClassificationName(classification)
                                    .build();

        memberQnADataService.saveQnAClassification(entity);
    }

    public void deleteQnAClassification(Long classificationId) {
        memberQnADataService.getQnAClassificationByIdOrElseIllegal(classificationId);
        memberQnADataService.deleteQnAClassificationById(classificationId);
    }
}
