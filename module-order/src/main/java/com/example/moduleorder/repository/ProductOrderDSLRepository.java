package com.example.moduleorder.repository;

import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.admin.business.AdminOrderDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductOrderDSLRepository {

    Page<ProductOrder> findByUserId(MemberOrderDTO memberOrderDTO, OrderPageDTO pageDTO, Pageable pageable);

    List<AdminOrderDTO> findAllOrderList(AdminOrderPageDTO pageDTO);

    Long findAllOrderListCount(AdminOrderPageDTO pageDTO);

    List<AdminOrderDTO> findNewOrderList(AdminOrderPageDTO pageDTO, LocalDateTime todayLastOrderTime);

    Long findNewOrderListCount(AdminOrderPageDTO pageDTO, LocalDateTime todayLastOrderTime);

    Page<ProductOrder> findProductOrderPageListByDay(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
