package com.example.moduleorder.usecase.admin;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.admin.business.AdminDailySalesDetailDTO;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDTO;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDetailListDTO;
import com.example.moduleorder.model.dto.admin.out.AdminDailySalesResponseDTO;
import com.example.moduleorder.model.dto.admin.out.AdminOrderResponseDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderReadUseCase {

    private final OrderDataService orderDataService;

    private final OrderDomainService orderDomainService;


    /**
     *
     * 아래와 같이 캐싱 데이터가 필요한 경우에 대해 개선.
     * 캐싱 조건은 각 기능별로 확립되어 있기 때문에 컨트롤러에서 사전 체크 이후 필요하다면 캐싱 조회를 하도록 하고
     * 여기에서는 TotalElements를 같이 받아서 처리하도록 하면
     * 캐싱 데이터가 필요없는 경우 굳이 모듈이 2번 호출될 필요가 없다.
     *
     * 그럼 의존성 문제도 해결.
     *
     * 이거 개선하면서 테스트 코드 수정 필요하게 될거니까 기존 코드 두고 아래처럼 더미 코드 만들어서 테스트 코드 수정한 뒤 처리하도록 하고
     * module-cache는 각 모듈에 대한 의존성 추가 필요.
     * 그리고 각 모듈에서 module-cache에 대한 의존성 제거 필요.
     *
     * 이후 build 테스트.
     */

    public PagingListDTO<AdminOrderResponseDTO> getAdminAllOrderList(AdminOrderPageDTO pageDTO, long totalElements) {
        if(pageDTO.keyword() == null && totalElements == 0) {
            PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(0L, pageDTO.page(), pageDTO.amount());
            return new PagingListDTO<>(Collections.emptyList(), pagingMappingDTO);
        }

        List<AdminOrderDTO> orderDTOList = orderDataService.getAdminAllOrderPageList(pageDTO);

        if(totalElements == 0)
            totalElements = orderDataService.findAllAdminOrderListCount(pageDTO);

        return mappingOrderDataAndPagingData(orderDTOList, totalElements, pageDTO);
    }

    private PagingListDTO<AdminOrderResponseDTO> mappingOrderDataAndPagingData(List<AdminOrderDTO> orderDTOList,
                                                                               long totalElements,
                                                                               AdminOrderPageDTO pageDTO) {
        List<Long> orderIds = orderDTOList.stream().map(AdminOrderDTO::orderId).toList();
        List<AdminOrderDetailListDTO> detailList = orderDataService.getAllAdminOrderDetailByOrderIds(orderIds);

        return orderDomainService.mapOrderResponsePagingList(orderDTOList, detailList, totalElements, pageDTO);
    }

    public PagingListDTO<AdminOrderResponseDTO> getAdminNewOrderList(AdminOrderPageDTO pageDTO) {
        LocalDateTime todayLastOrderTime = LocalDateTime.now()
                .withHour(16)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        List<AdminOrderDTO> orderDTOList = orderDataService.getAdminNewOrderPageList(pageDTO, todayLastOrderTime);

        Long totalElements = 0L;

        if(!orderDTOList.isEmpty())
            totalElements = orderDataService.getAdminNewOrderListCount(pageDTO, todayLastOrderTime);

        return mappingOrderDataAndPagingData(orderDTOList, totalElements, pageDTO);
    }

    public PagingListDTO<AdminDailySalesResponseDTO> getOrderListByDay(LocalDate termDate, int page) {
        LocalDateTime startDate = LocalDateTime.of(termDate, LocalTime.MIN);
        LocalDateTime endDate = LocalDateTime.of(termDate, LocalTime.MAX);

        Page<ProductOrder> orderList = orderDataService.getProductOrderPageListByDay(startDate, endDate, page);
        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(orderList.getTotalElements(), page, orderList.getSize());

        if(orderList.isEmpty())
            return new PagingListDTO<>(Collections.emptyList(), pagingMappingDTO);

        List<Long> orderIdList = orderList.stream().map(ProductOrder::getId).toList();
        List<AdminOrderDetailListDTO> orderDetailList = orderDataService.getAllAdminOrderDetailByOrderIds(orderIdList);

        List<AdminDailySalesResponseDTO> content = orderList.getContent()
                .stream()
                .map(v -> {
                    List<AdminDailySalesDetailDTO> detailContent = orderDetailList.stream()
                            .filter(orderDetail -> v.getId().equals(orderDetail.orderId()))
                            .map(AdminDailySalesDetailDTO::new)
                            .toList();

                    return new AdminDailySalesResponseDTO(v, detailContent);
                })
                .toList();

        return new PagingListDTO<>(content, pagingMappingDTO);
    }
}
