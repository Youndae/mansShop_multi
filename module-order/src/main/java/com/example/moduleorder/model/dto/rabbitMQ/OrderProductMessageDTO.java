package com.example.moduleorder.model.dto.rabbitMQ;

import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderProductMessageDTO {

    private List<OrderProductDTO> orderProductList;

    public OrderProductMessageDTO(ProductOrderDataDTO dto) {
        this.orderProductList = dto.orderProductList();
    }
}
