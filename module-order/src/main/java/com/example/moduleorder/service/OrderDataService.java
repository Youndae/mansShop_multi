package com.example.moduleorder.service;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.entity.ProductOrderDetail;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDTO;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDetailListDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
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
import java.time.LocalDateTime;
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

    public ProductOrderDetail getProductOrderDetailByIdOrElseIllegal(Long id) {
        return productOrderDetailRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }

    public void saveProductOrderDetail(ProductOrderDetail productOrderDetail) {
        productOrderDetailRepository.save(productOrderDetail);
    }

    public List<AdminOrderDTO> getAdminAllOrderPageList(AdminOrderPageDTO pageDTO) {
        return productOrderRepository.findAllOrderList(pageDTO);
    }

    public Long findAllAdminOrderListCount(AdminOrderPageDTO pageDTO) {
        return productOrderRepository.findAllOrderListCount(pageDTO);
    }

    public List<AdminOrderDetailListDTO> getAllAdminOrderDetailByOrderIds(List<Long> orderIds) {
        return productOrderDetailRepository.findAllAdminOrderDetailByOrderIds(orderIds);
    }

    public List<AdminOrderDTO> getAdminNewOrderPageList(AdminOrderPageDTO pageDTO, LocalDateTime todayLastOrderTime) {
        return productOrderRepository.findNewOrderList(pageDTO, todayLastOrderTime);
    }

    public Long getAdminNewOrderListCount(AdminOrderPageDTO pageDTO, LocalDateTime todayLastOrderTime) {
        return productOrderRepository.findNewOrderListCount(pageDTO, todayLastOrderTime);
    }

    public ProductOrder findProductOrderByIdOrElseIllegal(long orderId) {
        return productOrderRepository.findById(orderId).orElseThrow(IllegalArgumentException::new);
    }

    public Page<ProductOrder> getProductOrderPageListByDay(LocalDateTime startDate, LocalDateTime endDate, int page) {
        Pageable pageable = PageRequest.of(page - 1,
                PageAmount.ADMIN_DAILY_ORDER_AMOUNT.getAmount(),
                Sort.by("createdAt").descending());

        return productOrderRepository.findProductOrderPageListByDay(startDate, endDate, pageable);
    }
}
