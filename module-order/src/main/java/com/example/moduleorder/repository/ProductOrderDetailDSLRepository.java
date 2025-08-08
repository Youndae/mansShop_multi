package com.example.moduleorder.repository;

import com.example.moduleorder.model.dto.admin.business.AdminOrderDetailListDTO;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;

import java.util.List;

public interface ProductOrderDetailDSLRepository {

    List<OrderListDetailDTO> findAllOrderDetailByOrderIds(List<Long> orderIds);

    List<AdminOrderDetailListDTO> findAllAdminOrderDetailByOrderIds(List<Long> orderIds);
}
