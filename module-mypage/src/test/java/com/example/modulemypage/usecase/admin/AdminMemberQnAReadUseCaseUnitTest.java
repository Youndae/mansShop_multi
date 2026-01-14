package com.example.modulemypage.usecase.admin;

import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.modulemypage.service.MemberQnADataService;
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
public class AdminMemberQnAReadUseCaseUnitTest {

    @InjectMocks
    private AdminMemberQnAReadUseCase adminMemberQnAReadUseCase;

    @Mock
    private MemberQnADataService memberQnADataService;

    private List<AdminQnAListResponseDTO> getAdminQnAListResponse() {
        return IntStream.range(0, 30)
                .mapToObj(v -> new AdminQnAListResponseDTO(
                        v,
                        "testQnAClassificationName",
                        "testTitle" + v,
                        "tester" + v,
                        LocalDate.now(),
                        v % 2 == 0
                ))
                .toList();
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회")
    void getAllMemberQnAList() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(AdminListType.ALL.getType(), 1);
        List<AdminQnAListResponseDTO> fixture = getAdminQnAListResponse();
        List<AdminQnAListResponseDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = TestPaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(memberQnADataService.getAdminMemberQnAPageList(any(AdminQnAPageDTO.class))).thenReturn(fixtureList);

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, fixture.size())
        );

        verify(memberQnADataService, never()).findAllByAdminMemberQnACount(any(AdminQnAPageDTO.class));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회. 검색")
    void getAllMemberQnAListSearch() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("tester", AdminListType.ALL.getType(), 1);
        List<AdminQnAListResponseDTO> fixture = getAdminQnAListResponse();
        List<AdminQnAListResponseDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = TestPaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(memberQnADataService.getAdminMemberQnAPageList(any(AdminQnAPageDTO.class))).thenReturn(fixtureList);
        when(memberQnADataService.findAllByAdminMemberQnACount(any(AdminQnAPageDTO.class))).thenReturn((long) fixture.size());

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L)
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회")
    void getNewMemberQnAList() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(AdminListType.NEW.getType(), 1);
        List<AdminQnAListResponseDTO> fixture = getAdminQnAListResponse();
        List<AdminQnAListResponseDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = TestPaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(memberQnADataService.getAdminMemberQnAPageList(any(AdminQnAPageDTO.class))).thenReturn(fixtureList);
        when(memberQnADataService.findAllByAdminMemberQnACount(any(AdminQnAPageDTO.class))).thenReturn((long) fixture.size());

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L)
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회. 검색")
    void getNewMemberQnAListSearch() {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO("tester", AdminListType.NEW.getType(), 1);
        List<AdminQnAListResponseDTO> fixture = getAdminQnAListResponse();
        List<AdminQnAListResponseDTO> fixtureList = fixture.stream().limit(pageDTO.amount()).toList();
        int totalPages = TestPaginationUtils.getTotalPages(fixture.size(), pageDTO.amount());

        when(memberQnADataService.getAdminMemberQnAPageList(any(AdminQnAPageDTO.class))).thenReturn(fixtureList);
        when(memberQnADataService.findAllByAdminMemberQnACount(any(AdminQnAPageDTO.class))).thenReturn((long) fixture.size());

        PagingListDTO<AdminQnAListResponseDTO> result = assertDoesNotThrow(
                () -> adminMemberQnAReadUseCase.getMemberQnAList(pageDTO, 0L)
        );

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(fixtureList.size(), result.content().size());
        assertEquals(fixture.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
    }
}
