package com.example.modulemypage.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulemypage.model.dto.memberQnA.business.MemberQnADetailDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAListDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAModifyDataDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.service.MemberQnADataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberQnAReadUseCase {

    private final MemberQnADataService memberQnADataService;

    public Page<MemberQnAListDTO> getMemberQnAList(MyPagePageDTO pageDTO, String userId) {
        return memberQnADataService.findAllMemberQnAByUserId(pageDTO, userId);
    }

    public MemberQnADetailResponseDTO getMemberQnADetail(long qnaId, String nickname) {
        MemberQnADetailResponseDTO responseDTO = getMemberQnADetailData(qnaId);

        if(!responseDTO.writer().equals(nickname)){
            log.info("MemberQnA Detail writer not match nickname. writer : {}, nickname : {}", responseDTO.writer(), nickname);
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());
        }

        return responseDTO;
    }

    public MemberQnADetailResponseDTO getMemberQnADetailData(long qnaId) {
        MemberQnADetailDTO qnaDTO = memberQnADataService.findMemberQnADetailDTOById(qnaId);

        if(qnaDTO == null) {
            log.info("MemberQnA Detail data is null. qnaId = {}", qnaId);
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());
        }

        List<QnADetailReplyDTO> replyDTOList = memberQnADataService.getMemberQnADetailAllReplies(qnaId);

        return new MemberQnADetailResponseDTO(qnaDTO, replyDTOList);
    }

    public MemberQnAModifyDataDTO getModifyData(long qnaId, String userId) {
        MemberQnA memberQnA = memberQnADataService.findModifyDataByIdAndUserId(qnaId, userId);

        if(memberQnA == null) {
            log.info("MemberQnA Modify data is null. qnaId: {}, userId: {}", qnaId, userId);
            throw new IllegalArgumentException("MemberQnA Modify Data is not Found");
        }

        List<QnAClassificationDTO> classificationDTO = memberQnADataService.getAllQnAClassificationDTO();

        return new MemberQnAModifyDataDTO(memberQnA, classificationDTO);
    }

    public List<QnAClassificationDTO> getQnAClassification() {
        return memberQnADataService.getAllQnAClassificationDTO();
    }
}
