package com.example.modulemypage.repository;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulemypage.model.dto.memberQnA.business.MemberQnADetailDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberQnADSLRepository {

    Page<MemberQnAListDTO> findAllByUserId(String userId, Pageable pageable);

    MemberQnADetailDTO findByQnAId(long qnaId);

    MemberQnA findModifyDataByIdAndUserId(long qnaId, String userId);

    List<AdminQnAListResponseDTO> findAllByAdminMemberQnAPage(AdminQnAPageDTO pageDTO);

    Long countByAdminMemberQnA(AdminQnAPageDTO pageDTO);
}
