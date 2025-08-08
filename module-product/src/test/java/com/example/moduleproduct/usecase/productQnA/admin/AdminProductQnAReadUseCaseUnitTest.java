package com.example.moduleproduct.usecase.productQnA.admin;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.service.productQnA.ProductQnADataService;
import com.example.moduleproduct.usecase.admin.productQnA.AdminProductQnAReadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductQnAReadUseCaseUnitTest {

    @InjectMocks
    private AdminProductQnAReadUseCase adminProductQnAReadUseCase;

    @Mock
    private ProductQnADataService productQnADataService;

    private List<AdminQnAListResponseDTO> getAdminQnAListResponseDTO() {
        return IntStream.range(0, 30)
                .mapToObj(v -> new AdminQnAListResponseDTO(
                        v,
                        "OUTER",
                        "testTitle" + v,
                        "testWriter" + v,
                        LocalDate.now(),
                        v % 2 == 0
                ))
                .toList();
    }

    @Test
    @DisplayName(value = "전체 상품 문의 목록 조회.")
    void getProductQnAAllList() {
        List<AdminQnAListResponseDTO> allDataFixture = getAdminQnAListResponseDTO();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(AdminListType.ALL.getType(), 1);
        List<AdminQnAListResponseDTO> responseDTOFixture = allDataFixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(allDataFixture.size(), pageDTO.amount());

        when(productQnADataService.getAdminProductQnAList(any(AdminQnAPageDTO.class))).thenReturn(responseDTOFixture);

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, (long) allDataFixture.size())
        );

        verify(productQnADataService, never()).findAllAdminProductQnAListCount(any(AdminQnAPageDTO.class));

        assertNotNull(result);
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(allDataFixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(responseDTOFixture, result.content());
    }

    @Test
    @DisplayName(value = "전체 상품 문의 목록 조회. 검색")
    void getProductQnAAllListSearch() {
        List<AdminQnAListResponseDTO> allDataFixture = getAdminQnAListResponseDTO();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("tester", AdminListType.ALL.getType(), 1);
        List<AdminQnAListResponseDTO> responseDTOFixture = allDataFixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(allDataFixture.size(), pageDTO.amount());

        when(productQnADataService.getAdminProductQnAList(any(AdminQnAPageDTO.class))).thenReturn(responseDTOFixture);
        when(productQnADataService.findAllAdminProductQnAListCount(any(AdminQnAPageDTO.class))).thenReturn((long) allDataFixture.size());

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L)
        );

        assertNotNull(result);
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(allDataFixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(responseDTOFixture, result.content());
    }

    @Test
    @DisplayName(value = "미처리 상품 문의 목록 조회.")
    void getProductQnANewList() {
        List<AdminQnAListResponseDTO> allDataFixture = getAdminQnAListResponseDTO();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(AdminListType.NEW.getType(), 1);
        List<AdminQnAListResponseDTO> responseDTOFixture = allDataFixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(allDataFixture.size(), pageDTO.amount());

        when(productQnADataService.getAdminProductQnAList(any(AdminQnAPageDTO.class))).thenReturn(responseDTOFixture);
        when(productQnADataService.findAllAdminProductQnAListCount(any(AdminQnAPageDTO.class))).thenReturn((long) allDataFixture.size());

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L)
        );

        assertNotNull(result);
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(allDataFixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(responseDTOFixture, result.content());
    }

    @Test
    @DisplayName(value = "미처리 상품 문의 목록 조회. 검색")
    void getProductQnANewListSearch() {
        List<AdminQnAListResponseDTO> allDataFixture = getAdminQnAListResponseDTO();
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("tester", AdminListType.NEW.getType(), 1);
        List<AdminQnAListResponseDTO> responseDTOFixture = allDataFixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(allDataFixture.size(), pageDTO.amount());

        when(productQnADataService.getAdminProductQnAList(any(AdminQnAPageDTO.class))).thenReturn(responseDTOFixture);
        when(productQnADataService.findAllAdminProductQnAListCount(any(AdminQnAPageDTO.class))).thenReturn((long) allDataFixture.size());

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminProductQnAReadUseCase.getProductQnAList(pageDTO, 0L)
        );

        assertNotNull(result);
        assertEquals(pageDTO.amount(), result.content().size());
        assertEquals(allDataFixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(responseDTOFixture, result.content());
    }
}
