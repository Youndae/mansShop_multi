package com.example.moduleproduct.usecase.productReview.admin;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.service.productReview.ProductReviewDataService;
import com.example.moduleproduct.usecase.admin.productReview.AdminProductReviewReadUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductReviewReadUseCaseUnitTest {

    @InjectMocks
    private AdminProductReviewReadUseCase adminProductReviewReadUseCase;

    @Mock
    private ProductReviewDataService productReviewDataService;

    private List<AdminReviewDTO> getAdminReviewDTOList() {
        return IntStream.range(0, 30)
                .mapToObj(v -> new AdminReviewDTO(
                        v,
                        "testProductName" + v,
                        "tester" + v,
                        LocalDate.now(),
                        v % 2 == 0
                ))
                .toList();
    }

    @Test
    @DisplayName(value = "전체 리뷰 목록 조회")
    void getReviewList() {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(1);
        List<AdminReviewDTO> fixture = getAdminReviewDTOList();
        List<AdminReviewDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(productReviewDataService.getAdminProductReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn(fixtureList);

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(
                () -> adminProductReviewReadUseCase.getReviewList(pageDTO, AdminListType.ALL, fixture.size())
        );

        verify(productReviewDataService, never()).countByAdminReviewList(any(AdminReviewPageDTO.class), any());

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(fixtureList, result.content());
    }

    @Test
    @DisplayName(value = "전체 리뷰 목록 조회. 검색")
    void getReviewListSearch() {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO("tester", "user", 1);
        List<AdminReviewDTO> fixture = getAdminReviewDTOList();
        List<AdminReviewDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(productReviewDataService.getAdminProductReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn(fixtureList);
        when(productReviewDataService.countByAdminReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn((long) fixture.size());

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(
                () -> adminProductReviewReadUseCase.getReviewList(pageDTO, AdminListType.ALL, 0L)
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(fixtureList, result.content());
    }

    @Test
    @DisplayName(value = "미답변 리뷰 목록 조회")
    void getNewReviewList() {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(1);
        List<AdminReviewDTO> fixture = getAdminReviewDTOList();
        List<AdminReviewDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(productReviewDataService.getAdminProductReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn(fixtureList);
        when(productReviewDataService.countByAdminReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn((long) fixture.size());

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(
                () -> adminProductReviewReadUseCase.getReviewList(pageDTO, AdminListType.NEW, 0L)
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(fixtureList, result.content());
    }

    @Test
    @DisplayName(value = "전체 리뷰 목록 조회. 검색")
    void getNewReviewListSearch() {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO("tester", "user", 1);
        List<AdminReviewDTO> fixture = getAdminReviewDTOList();
        List<AdminReviewDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = PaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(productReviewDataService.getAdminProductReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn(fixtureList);
        when(productReviewDataService.countByAdminReviewList(any(AdminReviewPageDTO.class), any()))
                .thenReturn((long) fixture.size());

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(
                () -> adminProductReviewReadUseCase.getReviewList(pageDTO, AdminListType.NEW, 0L)
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(fixtureList, result.content());
    }

    @Test
    @DisplayName(value = "리뷰 상세 조회. 아이디가 잘못된 경우")
    void getReviewDetailWrongId() {

        when(productReviewDataService.getAdminReviewDetailById(anyLong())).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewReadUseCase.getReviewDetail(1L)
        );
    }
}
