package com.example.modulemypage.service;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.MemberQnAReply;
import com.example.modulecommon.model.entity.QnAClassification;
import com.example.modulemypage.model.dto.memberQnA.business.MemberQnADetailDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAListDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberQnADataService {

    private final MemberQnARepository memberQnARepository;

    private final MemberQnAReplyRepository memberQnAReplyRepository;

    private final QnAClassificationRepository qnAClassificationRepository;


    public Page<MemberQnAListDTO> findAllMemberQnAByUserId(MyPagePageDTO pageDTO, String userId) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                            pageDTO.amount(),
                                            Sort.by("id").descending()
                                    );
        return memberQnARepository.findAllByUserId(userId, pageable);
    }

    public QnAClassification findQnAClassificationByIdOrElseIllegal(long id) {
        return qnAClassificationRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public Long saveMemberQnA(MemberQnA saveEntity) {
        return memberQnARepository.save(saveEntity).getId();
    }

    public MemberQnADetailDTO findMemberQnADetailDTOById(long qnaId) {
        return memberQnARepository.findByQnAId(qnaId);
    }


    public List<QnADetailReplyDTO> getMemberQnADetailAllReplies(long qnaId) {
        return memberQnAReplyRepository.findAllByQnAId(qnaId);
    }

    public MemberQnA findMemberQnAByIdOrElseIllegal(long qnaId) {
        return memberQnARepository.findById(qnaId).orElseThrow(IllegalArgumentException::new);
    }

    public void saveMemberQnAReply(MemberQnAReply memberQnAReply) {
        memberQnAReplyRepository.save(memberQnAReply);
    }

    public MemberQnAReply findMemberQnAReplyByIdOrElseIllegal(long id) {
        return memberQnAReplyRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public MemberQnA findModifyDataByIdAndUserId(long qnaId, String userId) {
        return memberQnARepository.findModifyDataByIdAndUserId(qnaId, userId);
    }

    public List<QnAClassificationDTO> getAllQnAClassificationDTO() {
        return qnAClassificationRepository.getAllQnAClassificationDTO();
    }

    public void deleteMemberQnAById(long qnaId) {
        memberQnARepository.deleteById(qnaId);
    }

    public List<AdminQnAListResponseDTO> getAdminMemberQnAPageList(AdminQnAPageDTO pageDTO) {
        return memberQnARepository.findAllByAdminMemberQnAPage(pageDTO);
    }

    public Long findAllByAdminMemberQnACount(AdminQnAPageDTO pageDTO) {
        return memberQnARepository.countByAdminMemberQnA(pageDTO);
    }

    public void saveQnAClassification(QnAClassification entity) {
        qnAClassificationRepository.save(entity);
    }

    public QnAClassification getQnAClassificationByIdOrElseIllegal(Long classificationId) {
        return qnAClassificationRepository.findById(classificationId).orElseThrow(IllegalArgumentException::new);
    }

    public void deleteQnAClassificationById(Long classificationId) {
        qnAClassificationRepository.deleteById(classificationId);
    }
}
