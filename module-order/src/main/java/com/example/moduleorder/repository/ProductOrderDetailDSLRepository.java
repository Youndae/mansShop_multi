package com.example.moduleorder.repository;

import com.example.moduleorder.model.dto.business.OrderListDetailDTO;

import java.util.List;

public interface ProductOrderDetailDSLRepository {

    List<OrderListDetailDTO> findAllOrderDetailByOrderIds(List<Long> orderIds);
}
