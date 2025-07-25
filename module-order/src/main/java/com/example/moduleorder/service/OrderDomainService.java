package com.example.moduleorder.service;


import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecommon.customException.CustomOrderSessionExpiredException;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.moduleconfig.properties.FallbackProperties;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDomainService {

    private final FallbackProperties fallbackProperties;


    public ProductOrderDataDTO mapProductOrderDataDTO(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, LocalDateTime createdAt) {
        ProductOrder productOrder = paymentDTO.toOrderEntity(cartMemberDTO.uid(), createdAt);
        List<OrderProductDTO> orderProductDTOList = paymentDTO.orderProduct();
        List<String> orderProductIds = new ArrayList<>();
        List<Long> orderProductOptionIds = new ArrayList<>();
        int totalProductCount = 0; // 총 상품 수량. 기간별 매출 갱신이 필요

        for(OrderProductDTO data: orderProductDTOList) {
            productOrder.addDetail(data.toOrderDetailEntity());

            if(!orderProductIds.contains(data.getProductId()))
                orderProductIds.add(data.getProductId());

            orderProductOptionIds.add(data.getOptionId());
            totalProductCount += data.getDetailCount();
        }

        productOrder.setProductCount(totalProductCount);

        return new ProductOrderDataDTO(productOrder, orderProductDTOList, orderProductIds, orderProductOptionIds);
    }

    public List<OrderDataDTO> mapValidateFieldList(List<OrderProductDTO> orderProductDTOList, List<ProductOption> validateOptions) {
        List<OrderDataDTO> validateDTOFieldList = new ArrayList<>();

        for(OrderProductDTO dto: orderProductDTOList) {
            for(ProductOption option : validateOptions) {
                if(dto.getOptionId() == option.getId()) {
                    validateDTOFieldList.add(
                            new OrderDataDTO(
                                    dto.getProductId(),
                                    dto.getOptionId(),
                                    dto.getProductName(),
                                    option.getSize(),
                                    option.getColor(),
                                    dto.getDetailCount(),
                                    dto.getDetailPrice()
                            )
                    );

                    break;
                }
            }
        }

        return validateDTOFieldList;
    }

    public String extractOrderTokenValue(Cookie orderTokenCookie) {
        if(orderTokenCookie == null) {
            log.warn("Order Session Expired. orderToken is null");
            throw new CustomOrderSessionExpiredException(
                    ErrorCode.ORDER_SESSION_EXPIRED,
                    ErrorCode.ORDER_SESSION_EXPIRED.getMessage()
            );
        }

        return orderTokenCookie.getValue();
    }

    public boolean validateOrderData(PreOrderDataVO cachingOrderData, OrderDataResponseDTO validateDTO, String userId) {
        if(!cachingOrderData.userId().equals(userId) ||
                cachingOrderData.totalPrice() != validateDTO.totalPrice()){
            log.error("Order Data Validation Failed. UserId or TotalPrice is different - userId: {}, validatePrice: {}, requestPrice: {}",
                    userId, cachingOrderData.totalPrice(), validateDTO.totalPrice());
            return false;
        }

        List<OrderItemVO> preOrderData = cachingOrderData.orderData();
        List<OrderDataDTO> requestData = validateDTO.orderData();

        for(OrderDataDTO data : requestData) {
            OrderItemVO dataVO = data.toOrderItemVO();
            if(!preOrderData.contains(dataVO))
                return false;
        }

        return true;
    }

    public FailedOrderDTO getFailedOrderDTO(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, Exception e) {
        return new FailedOrderDTO(paymentDTO, cartMemberDTO, LocalDateTime.now(), e.getMessage());
    }

    public String getFallbackOrderKey(FallbackMapKey fallbackMapKey) {
        String randomString = UUID.randomUUID().toString();
        String keyPrefix = fallbackProperties.getRedis().get(fallbackMapKey.getKey()).getPrefix();

        return keyPrefix.concat(randomString);
    }

    public OrderDataResponseDTO mappingOrderResponseDTO(List<OrderProductRequestDTO> optionIdAndCountDTO, List<OrderProductInfoDTO> orderInfoList) {
        int totalPrice = 0;
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();

        for(OrderProductInfoDTO data : orderInfoList) {
            for(OrderProductRequestDTO dto : optionIdAndCountDTO){
                if(data.optionId() == dto.optionId()) {
                    OrderDataDTO orderData = new OrderDataDTO(data, dto.count());
                    orderDataDTOList.add(orderData);
                    totalPrice += orderData.price();
                }
            }
        }

        return new OrderDataResponseDTO(orderDataDTOList, totalPrice);
    }

    public List<OrderListDTO> mapOrderListDTO(List<ProductOrder> order, List<OrderListDetailDTO> detailDTOList) {
        List<OrderListDTO> responseList = new ArrayList<>();

        for(ProductOrder data : order){
            List<OrderListDetailDTO> orderDetailList = detailDTOList.stream()
                    .filter(dto -> data.getId() == dto.orderId())
                    .toList();

            responseList.add(new OrderListDTO(data, orderDetailList));
        }

        return responseList;
    }
}
