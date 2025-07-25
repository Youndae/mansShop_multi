package com.example.moduleorder.usecase;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReadUseCase {

    private final OrderDataService orderDataService;

    private final OrderDomainService orderDomainService;

    public PagingListDTO<OrderListDTO> getOrderList(OrderPageDTO orderPageDTO, MemberOrderDTO memberOrderDTO) {
        Page<ProductOrder> order = orderDataService.findAllByUserId(memberOrderDTO, orderPageDTO);
        List<OrderListDTO> contentList = new ArrayList<>();

        if(!order.getContent().isEmpty()) {
            List<Long> orderIds = order.getContent().stream().map(ProductOrder::getId).toList();
            List<OrderListDetailDTO> detailDTOList = orderDataService.getAllOrderDetailByOrderIds(orderIds);
            contentList = orderDomainService.mapOrderListDTO(order.getContent(), detailDTOList);
        }

        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(order);

        return new PagingListDTO<>(contentList, pagingMappingDTO);
    }
}
