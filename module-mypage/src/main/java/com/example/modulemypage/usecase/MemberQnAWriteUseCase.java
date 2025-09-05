package com.example.modulemypage.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.MemberQnAReply;
import com.example.modulecommon.model.entity.QnAClassification;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAInsertDTO;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAModifyDTO;
import com.example.modulemypage.service.MemberQnADataService;
import com.example.modulemypage.service.MemberQnADomainService;
import com.example.moduleuser.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberQnAWriteUseCase {

    private final UserDataService userDataService;

    private final MemberQnADataService memberQnADataService;

    private final MemberQnADomainService memberQnADomainService;

    public Long postMemberQnA(MemberQnAInsertDTO insertDTO, String userId) {
        Member member = userDataService.getMemberByUserIdOrElseIllegal(userId);
        QnAClassification qnAClassification = memberQnADataService.findQnAClassificationByIdOrElseIllegal(insertDTO.classificationId());
        MemberQnA saveEntity = memberQnADomainService.buildInsertMemberQnA(insertDTO, qnAClassification, member);

        return memberQnADataService.saveMemberQnA(saveEntity);
    }

    public void postMemberQnAReply(QnAReplyInsertDTO insertDTO, String userId) {
        Member member = userDataService.getMemberByUserIdFetchAuthsOrElseIllegal(userId);
        MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(insertDTO.qnaId());
        boolean isAdmin = member.getAuths().size() > 1;

        if(!isAdmin && !member.getUserId().equals(memberQnA.getMember().getUserId())) {
            log.info("PostMemberQnAReply writer not match or not admin. userId: {}, writer: {} ", userId, memberQnA.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        memberQnA.setMemberQnAStat(isAdmin);
        memberQnADataService.saveMemberQnA(memberQnA);

        MemberQnAReply memberQnAReply = memberQnADomainService.buildInsertMemberQnaReply(member, memberQnA, insertDTO.content());

        memberQnADataService.saveMemberQnAReply(memberQnAReply);
    }

    public void patchMemberQnAReply(QnAReplyPatchDTO replyDTO, String userId) {
        MemberQnAReply memberQnAReply = memberQnADataService.findMemberQnAReplyByIdOrElseIllegal(replyDTO.replyId());

        if(!memberQnAReply.getMember().getUserId().equals(userId)){
            log.info("PatchMemberQnAReply writer not match. userId: {}, writer: {} ", userId, memberQnAReply.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        memberQnAReply.setReplyContent(replyDTO.content());

        memberQnADataService.saveMemberQnAReply(memberQnAReply);
    }

    public void patchMemberQnA(MemberQnAModifyDTO modifyDTO, String userId) {
        MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(modifyDTO.qnaId());

        if(!memberQnA.getMember().getUserId().equals(userId)){
            log.info("PatchMemberQnA writer not match. userId: {}, writer: {}", userId, memberQnA.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        QnAClassification qnAClassification = memberQnADataService.findQnAClassificationByIdOrElseIllegal(modifyDTO.classificationId());

        memberQnA.setModifyData(modifyDTO.title(), modifyDTO.content(), qnAClassification);

        memberQnADataService.saveMemberQnA(memberQnA);
    }

    public void deleteMemberQnA(long qnaId, String userId) {
        MemberQnA memberQnA = memberQnADataService.findMemberQnAByIdOrElseIllegal(qnaId);

        if(!memberQnA.getMember().getUserId().equals(userId)){
            log.info("DeleteMemberQnA writer not match. userId: {}, writer: {}", userId, memberQnA.getMember().getUserId());
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        memberQnADataService.deleteMemberQnAById(qnaId);
    }
}
