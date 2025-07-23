package com.example.moduleproduct.service.main;

import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.moduleproduct.model.dto.main.business.MainListDTO;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MainDomainService {

    public List<MainListResponseDTO> mappingMainListData(List<MainListDTO> dto) {
        return dto.stream()
                .map(MainListResponseDTO::new)
                .toList();
    }

    public PagingMappingDTO getMainPagingMappingDTO(Page<MainListDTO> dto) {
        return PagingMappingDTO.builder()
                            .totalElements(dto.getTotalElements())
                            .totalPages(dto.getTotalPages())
                            .empty(dto.isEmpty())
                            .number(dto.getNumber())
                            .build();
    }
}
