package com.example.modulemypage.usecase.admin;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulemypage.service.MemberQnADataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberQnAReadUseCase {

    private final MemberQnADataService memberQnADataService;

    public PagingListDTO<AdminQnAListResponseDTO> getMemberQnAList(AdminQnAPageDTO pageDTO, long totalElements) {
        List<AdminQnAListResponseDTO> responseContent = memberQnADataService.getAdminMemberQnAPageList(pageDTO);

        if(!responseContent.isEmpty() && !(pageDTO.listType().equals(AdminListType.ALL.getType()) && pageDTO.keyword() == null))
            totalElements = memberQnADataService.findAllByAdminMemberQnACount(pageDTO);

        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(totalElements, pageDTO.page(), pageDTO.amount());

        return new PagingListDTO<>(responseContent, pagingMappingDTO);
    }
}
