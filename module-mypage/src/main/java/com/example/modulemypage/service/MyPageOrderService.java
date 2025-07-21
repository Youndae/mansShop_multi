package com.example.modulemypage.service;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulemypage.model.dto.out.MyPageOrderDTO;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageOrderService {

    private final ProductOrderRepository productOrderRepository;

    private final ProductOrderDetailRepository productOrderDetailRepository;

    /**
     *
     * @param pageDTO
     * @param memberOrderDTO
     *
     * 주문 목록 조회.
     * OrderPageDTO에서는 term을 같이 받는데 3, 6, 12, all 네가지로 받는다.
     * 각 개월수를 의미.
     * 해당 개월수에 맞는 데이터 리스트를 조회.
     *
     * 주문 테이블과 주문 상세 테이블은 데이터가 빠르게 쌓이기 때문에 페이징을 직접 처리.
     */
    public PagingListDTO<MyPageOrderDTO> getOrderList(OrderPageDTO pageDTO, MemberOrderDTO memberOrderDTO) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                pageDTO.orderAmount(),
                Sort.by("orderId").descending());

        Page<ProductOrder> order = productOrderRepository.findByUserId(memberOrderDTO, pageDTO, pageable);
        List<MyPageOrderDTO> contentList = new ArrayList<>();

        if(!order.getContent().isEmpty()) {
            List<Long> orderIds = order.getContent().stream().map(ProductOrder::getId).toList();
            List<OrderListDetailDTO> detailDTOList = productOrderDetailRepository.findByDetailList(orderIds);

            for(ProductOrder data : order.getContent()) {
                List<OrderListDetailDTO> detailList = detailDTOList.stream()
                        .filter(dto -> data.getId() == dto.orderId())
                        .toList();

                contentList.add(new MyPageOrderDTO(data, detailList));
            }
        }

        PagingMappingDTO pagingMappingDTO = PagingMappingDTO.builder()
                .empty(order.isEmpty())
                .number(order.getNumber())
                .totalPages(order.getTotalPages())
                .totalElements(order.getTotalElements())
                .build();

        return new PagingListDTO<>(contentList, pagingMappingDTO);
    }
}
