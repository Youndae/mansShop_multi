package com.example.modulemypage.usecase.admin;

import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.QnAClassification;
import com.example.modulecommon.model.enumuration.Result;
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

    public String patchMemberQnAComplete(long qnaId) {
        MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(qnaId);
        patchMemberQnAStatusAndSave(memberQnA);
        return Result.OK.getResultKey();
    }

    private void patchMemberQnAStatusAndSave(MemberQnA memberQnA) {
        memberQnA.setMemberQnAStat(true);
        memberQnADataService.saveMemberQnA(memberQnA);
    }

    public String postMemberQnAReply(QnAReplyInsertDTO insertDTO, String userId) {
        String postReplyResult = memberQnAWriteUseCase.postMemberQnAReply(insertDTO, userId);

        if(postReplyResult.equals(Result.OK.getResultKey())) {
            MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(insertDTO.qnaId());
            patchMemberQnAStatusAndSave(memberQnA);

            memberQnAExternalService.sendMemberQnANotification(insertDTO.qnaId(), memberQnA);

            return Result.OK.getResultKey();
        }else {
            throw new IllegalArgumentException("PostMemberQnAReply Result is not OK.");
        }
    }

    public String postQnAClassification(String classification) {
        QnAClassification entity = QnAClassification.builder()
                                    .qnaClassificationName(classification)
                                    .build();

        memberQnADataService.saveQnAClassification(entity);

        return Result.OK.getResultKey();
    }

    public String deleteQnAClassification(Long classificationId) {
        memberQnADataService.getQnAClassificationByIdOrElseIllegal(classificationId);
        memberQnADataService.deleteQnAClassificationById(classificationId);

        return Result.OK.getResultKey();
    }
}
