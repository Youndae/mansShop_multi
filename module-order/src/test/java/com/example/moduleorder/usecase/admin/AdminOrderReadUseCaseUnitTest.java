package com.example.moduleorder.usecase.admin;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleorder.model.dto.admin.business.AdminDailySalesDetailDTO;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDTO;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDetailListDTO;
import com.example.moduleorder.model.dto.admin.out.AdminDailySalesResponseDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminOrderReadUseCaseUnitTest {

    @InjectMocks
    private AdminOrderReadUseCase adminOrderReadUseCase;

    @Mock
    private OrderDataService orderDataService;

    @Mock
    private OrderDomainService orderDomainService;

    private List<ProductOrder> getOrderFixtureList() {
        List<Member> memberFixtureList = MemberAndAuthFixture.createDefaultMember(30).memberList();
        List<ProductOption> productOptionFixtureList = ProductFixture.createDefaultProductByOUTER(20)
                .stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();

        return ProductOrderFixture.createSaveProductOrder(memberFixtureList, productOptionFixtureList);
    }

    /**
     * 전체 mocking으로 인해 totalElement 조회 분기만 체크
     */
    @Test
    @DisplayName(value = "모든 주문 목록 조회. 캐싱된 totalElements를 조회하는 경우")
    void getAllOrderList() {
        List<ProductOrder> orderFixtureList = getOrderFixtureList();
        List<AdminOrderDTO> resultOrderDTO = orderFixtureList.stream()
                .map(v ->
                        new AdminOrderDTO(v.getId(),
                                v.getRecipient(),
                                v.getMember().getUserId(),
                                v.getOrderPhone(),
                                v.getCreatedAt(),
                                v.getOrderAddress(),
                                v.getOrderStat()
                        )
                )
                .limit(20)
                .toList();

        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(null, null, 1);

        when(orderDataService.getAdminAllOrderPageList(any())).thenReturn(resultOrderDTO);
        when(orderDataService.getAllAdminOrderDetailByOrderIds(anyList())).thenReturn(Collections.emptyList());
        when(orderDomainService.mapOrderResponsePagingList(anyList(), anyList(), anyLong(), any())).thenReturn(mock(PagingListDTO.class));

        assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, orderFixtureList.size()));

        verify(orderDataService, never()).findAllAdminOrderListCount(any(AdminOrderPageDTO.class));
    }

    /**
     * 전체 mocking으로 인해 totalElement 조회 분기만 체크
     */
    @Test
    @DisplayName(value = "모든 주문 목록 조회. DB에서 직접 totalElements를 조회하는 경우")
    void getAllOrderListSearch() {
        List<ProductOrder> orderFixtureList = getOrderFixtureList();
        List<AdminOrderDTO> resultOrderDTO = orderFixtureList.stream()
                .map(v ->
                        new AdminOrderDTO(v.getId(),
                                v.getRecipient(),
                                v.getMember().getUserId(),
                                v.getOrderPhone(),
                                v.getCreatedAt(),
                                v.getOrderAddress(),
                                v.getOrderStat()
                        )
                )
                .limit(20)
                .toList();

        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO("tester", "recipient", 1);

        when(orderDataService.getAdminAllOrderPageList(any())).thenReturn(resultOrderDTO);
        when(orderDataService.findAllAdminOrderListCount(any(AdminOrderPageDTO.class))).thenReturn(0L);
        when(orderDataService.getAllAdminOrderDetailByOrderIds(anyList())).thenReturn(Collections.emptyList());
        when(orderDomainService.mapOrderResponsePagingList(anyList(), anyList(), anyLong(), any())).thenReturn(mock(PagingListDTO.class));

        assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, 0L));

        verify(orderDataService).findAllAdminOrderListCount(any(AdminOrderPageDTO.class));
    }

    @Test
    @DisplayName(value = "모든 주문 목록 조회. 주문 데이터가 존재하지 않는 경우")
    void getAllOrderListEmpty() {
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(null, null, 1);

        assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminAllOrderList(pageDTO, 0L));

        verify(orderDataService, never()).getAdminAllOrderPageList(any());
        verify(orderDataService, never()).findAllAdminOrderListCount(any(AdminOrderPageDTO.class));
        verify(orderDataService, never()).getAllAdminOrderDetailByOrderIds(anyList());
        verify(orderDomainService, never()).mapOrderResponsePagingList(anyList(), anyList(), anyLong(), any());
        verify(orderDataService, never()).findAllAdminOrderListCount(any(AdminOrderPageDTO.class));
    }

    /**
     * count 쿼리 실행 여부만 체크
     */
    @Test
    @DisplayName(value = "미처리 주문 목록 조회.")
    void getNewOrderList() {
        List<ProductOrder> orderFixtureList = getOrderFixtureList();
        List<AdminOrderDTO> resultOrderDTO = orderFixtureList.stream()
                .map(v ->
                        new AdminOrderDTO(v.getId(),
                                v.getRecipient(),
                                v.getMember().getUserId(),
                                v.getOrderPhone(),
                                v.getCreatedAt(),
                                v.getOrderAddress(),
                                v.getOrderStat()
                        )
                )
                .limit(20)
                .toList();
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(null, null, 1);

        when(orderDataService.getAdminNewOrderPageList(any(AdminOrderPageDTO.class), any(LocalDateTime.class))).thenReturn(resultOrderDTO);
        when(orderDataService.getAdminNewOrderListCount(any(AdminOrderPageDTO.class), any(LocalDateTime.class))).thenReturn((long) orderFixtureList.size());
        when(orderDataService.getAllAdminOrderDetailByOrderIds(anyList())).thenReturn(Collections.emptyList());
        when(orderDomainService.mapOrderResponsePagingList(anyList(), anyList(), anyLong(), any())).thenReturn(mock(PagingListDTO.class));

        assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminNewOrderList(pageDTO));
    }

    /**
     * count 쿼리 실행 여부만 체크
     */
    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 주문 데이터가 존재하지 않는 경우")
    void getNewOrderListEmpty() {
        AdminOrderPageDTO pageDTO = new AdminOrderPageDTO(null, null, 1);

        when(orderDataService.getAdminNewOrderPageList(any(AdminOrderPageDTO.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(orderDataService.getAllAdminOrderDetailByOrderIds(anyList())).thenReturn(Collections.emptyList());
        when(orderDomainService.mapOrderResponsePagingList(anyList(), anyList(), anyLong(), any())).thenReturn(mock(PagingListDTO.class));

        assertDoesNotThrow(() -> adminOrderReadUseCase.getAdminNewOrderList(pageDTO));

        verify(orderDataService, never()).getAdminNewOrderListCount(any(AdminOrderPageDTO.class), any());
    }

    @Test
    @DisplayName(value = "선택일자의 모든 주문 목록 조회")
    void getOrderListByDay() {
        List<ProductOrder> orderFixtureList = getOrderFixtureList();
        List<ProductOrder> pageList = orderFixtureList.stream().limit(20).toList();
        Pageable pageable = PageRequest.of(0,
                PageAmount.ADMIN_DAILY_ORDER_AMOUNT.getAmount(),
                Sort.by("createdAt").descending());
        Page<ProductOrder> orderPageListFixture = new PageImpl<>(pageList, pageable, orderFixtureList.size());
        PagingMappingDTO pagingMappingDTO = PagingMappingDTO.builder()
                .totalElements(orderPageListFixture.getTotalElements())
                .number(orderPageListFixture.getNumber())
                .empty(orderPageListFixture.isEmpty())
                .totalPages(orderPageListFixture.getTotalPages())
                .build();
        List<AdminOrderDetailListDTO> detailListFixture = pageList.stream()
                                                                .flatMap(v ->
                                                                        v.getProductOrderDetailList().stream()
                                                                )
                                                                .map(v ->
                                                                        new AdminOrderDetailListDTO(
                                                                            v.getProductOrder().getId(),
                                                                            v.getProduct().getClassification().getId(),
                                                                            v.getProduct().getProductName(),
                                                                            v.getProductOption().getSize(),
                                                                            v.getProductOption().getColor(),
                                                                            v.getOrderDetailCount(),
                                                                            v.getOrderDetailPrice(),
                                                                            v.isOrderReviewStatus()
                                                                ))
                                                                .toList();
        List<AdminDailySalesResponseDTO> resultFixture = new ArrayList<>();

        for(ProductOrder order : pageList){
            List<AdminDailySalesDetailDTO> detailDTOList = new ArrayList<>();

            for(AdminOrderDetailListDTO detail : detailListFixture){
                if(order.getId().equals(detail.orderId())) {
                    detailDTOList.add(new AdminDailySalesDetailDTO(detail));
                }
            }

            resultFixture.add(new AdminDailySalesResponseDTO(order, detailDTOList));
        }

        when(orderDataService.getProductOrderPageListByDay(any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(orderPageListFixture);
        when(orderDataService.getAllAdminOrderDetailByOrderIds(anyList())).thenReturn(detailListFixture);

        PagingListDTO<AdminDailySalesResponseDTO> result = assertDoesNotThrow(
                () -> adminOrderReadUseCase.getOrderListByDay(LocalDate.of(2025, 1, 1), 1)
        );

        assertNotNull(result);
        assertEquals(resultFixture.size(), result.content().size());
        assertEquals(resultFixture, result.content());
        assertEquals(pagingMappingDTO.getTotalElements(), result.pagingData().getTotalElements());
        assertEquals(pagingMappingDTO.getTotalPages(), result.pagingData().getTotalPages());
        assertFalse(result.pagingData().isEmpty());
    }

    @Test
    @DisplayName(value = "선택일자의 모든 주문 목록 조회. 데이터가 없는 경우")
    void getOrderListByDayEmpty() {
        Pageable pageable = PageRequest.of(0,
                PageAmount.ADMIN_DAILY_ORDER_AMOUNT.getAmount(),
                Sort.by("createdAt").descending());
        Page<ProductOrder> orderPageListFixture = new PageImpl<>(Collections.emptyList(), pageable, 0L);
        when(orderDataService.getProductOrderPageListByDay(any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(orderPageListFixture);

        PagingListDTO<AdminDailySalesResponseDTO> result = assertDoesNotThrow(
                () -> adminOrderReadUseCase.getOrderListByDay(LocalDate.of(2025, 1, 1), 1)
        );

        verify(orderDataService, never()).getAllAdminOrderDetailByOrderIds(anyList());

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
        assertTrue(result.pagingData().isEmpty());
    }
}
