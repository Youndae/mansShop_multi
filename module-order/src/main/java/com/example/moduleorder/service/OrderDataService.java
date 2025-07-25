package com.example.moduleorder.service;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDataService {

    private final ProductOrderRepository productOrderRepository;

    private final ProductOrderDetailRepository productOrderDetailRepository;

    private final RedisTemplate<String, PreOrderDataVO> orderRedisTemplate;

    private final RedisTemplate<String, FailedOrderDTO> failedOrderRedisTemplate;

    public PreOrderDataVO getCachingOrderData(String orderTokenValue) {
        return orderRedisTemplate.opsForValue().get(orderTokenValue);
    }

    public void deleteOrderTokenData(String orderTokenValue) {
        orderRedisTemplate.delete(orderTokenValue);
    }

    public void saveProductOrder(ProductOrder order) {
        productOrderRepository.save(order);
    }

    public void setFailedOrderToRedis(String orderKey, FailedOrderDTO failedDTO) {
        failedOrderRedisTemplate.opsForValue().set(orderKey, failedDTO);
    }

    public void setOrderValidateDataToRedis(String token, PreOrderDataVO preOrderDataVO) {
        orderRedisTemplate.opsForValue().set(token, preOrderDataVO, Duration.ofMinutes(10));
    }

    public Page<ProductOrder> findAllByUserId(MemberOrderDTO memberOrderDTO, OrderPageDTO pageDTO) {
        Pageable pageable = PageRequest.of(pageDTO.pageNum() - 1,
                                                    pageDTO.amount(),
                                                    Sort.by("orderId").descending()
                                            );

        return productOrderRepository.findByUserId(memberOrderDTO, pageDTO, pageable);
    }

    public List<OrderListDetailDTO> getAllOrderDetailByOrderIds(List<Long> orderIds) {
        return productOrderDetailRepository.findAllOrderDetailByOrderIds(orderIds);
    }
}
