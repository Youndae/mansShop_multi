package com.example.moduleorder.usecase;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.service.CartDataService;
import com.example.modulecart.service.CartDomainService;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.customException.CustomOrderDataFailedException;
import com.example.modulecommon.customException.CustomOrderSessionExpiredException;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.service.OrderCookieWriter;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderDomainService;
import com.example.moduleorder.service.OrderExternalService;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderWriteUseCase {

    private static final Logger failedOrderLogger = LoggerFactory.getLogger("com.example.moduleorder.order.failed");

    private final CartDomainService cartDomainService;

    private final OrderDataService orderDataService;

    private final OrderDomainService orderDomainService;

    private final OrderExternalService orderExternalService;

    private final ProductDataService productDataService;

    private final OrderCookieWriter orderCookieWriter;

    private final CartDataService cartDataService;

    /**
     * 결제 이후 데이터 처리
     */
    @Transactional(rollbackFor = Exception.class)
    public void orderDataProcessAfterPayment(PaymentDTO paymentDTO,
                                             Cookie cartCookie,
                                             String userId,
                                             Cookie orderTokenCookie,
                                             HttpServletResponse response) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);
        boolean successFlag = false;
        ProductOrderDataDTO productOrderDataDTO = orderDomainService.mapProductOrderDataDTO(paymentDTO, cartMemberDTO, LocalDateTime.now());

        try {
            List<ProductOption> validateOptions = productDataService.getProductOptionListByIds(productOrderDataDTO.orderOptionIds());
            List<OrderDataDTO> validateDTOFieldList = orderDomainService.mapValidateFieldList(paymentDTO.orderProduct(), validateOptions);
            OrderDataResponseDTO validateDTO = new OrderDataResponseDTO(validateDTOFieldList, paymentDTO.totalPrice());
            validateOrderData(validateDTO, cartMemberDTO.uid(), orderTokenCookie, response);
        }catch (Exception e) {
            log.error("OrderService.payment :: payment Order validation Failed. cartMemberDTO : {}, submittedPaymentDTO : {}",
                    cartMemberDTO, paymentDTO);
            throw new CustomOrderSessionExpiredException(
                    ErrorCode.ORDER_SESSION_EXPIRED,
                    ErrorCode.ORDER_SESSION_EXPIRED.getMessage()
            );
        }

        try {
            ProductOrder order = productOrderDataDTO.productOrder();
            orderDataService.saveProductOrder(order);
            successFlag = true;

            orderExternalService.sendOrderMessageQueue(paymentDTO, cartMemberDTO, productOrderDataDTO, order);
        }catch (Exception e) {
            log.error("payment Error : ", e);
            if(!successFlag) {
                log.error("handleFallback call");
                // productOrder 저장도 실패한 경우
                handleOrderFallback(paymentDTO, cartMemberDTO, e);
                throw new CustomOrderDataFailedException(ErrorCode.ORDER_DATA_FAILED, ErrorCode.ORDER_DATA_FAILED.getMessage());
            }else {
                log.error("payment Message Queue Error : ", e);
                // productOrder 저장은 성공했으나 RabbitMQ를 통한 나머지 데이터 처리가 실패한 경우
                // RabbitMQ 연결 또는 장애 이슈 등으로 메시지가 아예 전달이 안된 경우
                // 메시지가 전달되었지만 실패하는 경우는 DLQ로 넘어가기 때문에 여기까지 넘어오지 않음.
                handleOrderMQFallback(paymentDTO, cartMemberDTO, e);
            }
        }finally {
            orderCookieWriter.deleteOrderTokenCookie(response);
        }
    }

    private void handleOrderFallback(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, Exception e) {
        orderRedisFallbackProcess(paymentDTO, cartMemberDTO, e, FallbackMapKey.ORDER);
    }

    private void handleOrderMQFallback(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, Exception e) {
        orderRedisFallbackProcess(paymentDTO, cartMemberDTO, e, FallbackMapKey.ORDER_MESSAGE);
    }

    private void orderRedisFallbackProcess(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, Exception e, FallbackMapKey fallbackMapKey) {
        FailedOrderDTO failedDTO = orderDomainService.getFailedOrderDTO(paymentDTO, cartMemberDTO, e);
        ObjectMapper om = new ObjectMapper();

        try {
            String orderKey = orderDomainService.getFallbackOrderKey(fallbackMapKey);

            orderDataService.setFailedOrderToRedis(orderKey, failedDTO);
        }catch (Exception e1) {
            try {
                failedOrderLogger.error("handleOrderFallback Error :: request Data : {}", om.writeValueAsString(failedDTO));
            }catch (JsonProcessingException e2) {
                failedOrderLogger.error("handleOrderFallback Error :: JsonProcessingException - request Data : {}", failedDTO);
            }
            log.error("handleOrderFallback Error Message : ", e1);
        }
    }

    /**
     *
     * @param validateDTO
     * @param userId
     * @param orderTokenCookie
     * @param response
     *
     * 결제하고자 하는 데이터와 Redis에 캐싱된 주문 데이터 비교 검증
     */
    public void validateOrderData(OrderDataResponseDTO validateDTO,
                                  String userId,
                                  Cookie orderTokenCookie,
                                  HttpServletResponse response) {
        String orderTokenValue = orderDomainService.extractOrderTokenValue(orderTokenCookie);
        PreOrderDataVO cachingOrderData = orderDataService.getCachingOrderData(orderTokenValue);
        ObjectMapper om = new ObjectMapper();
        if(cachingOrderData == null) {
            log.warn("Order Session Expired. Validate Data is null - token: {}", orderTokenValue);
            orderCookieWriter.deleteOrderTokenCookie(response);
            throw new CustomOrderSessionExpiredException(
                    ErrorCode.ORDER_SESSION_EXPIRED,
                    ErrorCode.ORDER_SESSION_EXPIRED.getMessage()
            );
        }

        if(!orderDomainService.validateOrderData(cachingOrderData, validateDTO, userId)) {
            try {
                orderCookieWriter.deleteOrderTokenCookie(response);
                orderDataService.deleteOrderTokenData(orderTokenValue);
                log.error("Order Data Validation Failed - token: {}, userId: {}, submittedData: {}, redisData: {}",
                        orderTokenValue, userId, om.writeValueAsString(validateDTO), om.writeValueAsString(cachingOrderData));
            }catch (JsonProcessingException e) {
                log.error("OrderService.validateOrder :: JsonProcessingException - Order Data Validation Failed - token: {}, userId: {}, submittedData: {}, redisData: {}",
                        orderTokenValue, userId, validateDTO, cachingOrderData);
            }
            throw new CustomOrderSessionExpiredException(
                    ErrorCode.ORDER_SESSION_EXPIRED,
                    ErrorCode.ORDER_SESSION_EXPIRED.getMessage()
            );
        }
    }

    public String retryFailedOrder(FailedOrderDTO failedOrderDTO, FallbackMapKey fallbackMapKey) {
        try {
            ProductOrderDataDTO productOrderDataDTO = orderDomainService.mapProductOrderDataDTO(failedOrderDTO.paymentDTO(), failedOrderDTO.cartMemberDTO(), failedOrderDTO.failedTime());
            ProductOrder order = productOrderDataDTO.productOrder();
            orderExternalService.retryFailedOrder(failedOrderDTO, productOrderDataDTO, order, fallbackMapKey);

            return Result.OK.getResultKey();
        }catch (Exception e) {
            log.error("retry payment Message Queue Error : ", e);
            //재시도가 다시한번 연결 또는 RabbitMQ 장애로 인해 실패한다면
            //다시 Redis에 적재
            handleOrderMQFallback(failedOrderDTO.paymentDTO(), failedOrderDTO.cartMemberDTO(), e);
            throw new CustomOrderDataFailedException(ErrorCode.ORDER_DATA_FAILED, ErrorCode.ORDER_DATA_FAILED.getMessage());
        }
    }

    /**
     *
     * @param optionIdAndCountDTO
     * @param orderTokenCookie
     * @param response
     * @param userId
     *
     * 상품 상세 페이지에서 주문 요청 시 상품 정보 조회.
     * 상품 정보 및 가격, 주문 토큰 생성 및 반환
     */
    public OrderDataResponseDTO getProductOrderData(List<OrderProductRequestDTO> optionIdAndCountDTO,
                                                    Cookie orderTokenCookie,
                                                    HttpServletResponse response,
                                                    String userId) {
        List<OrderProductInfoDTO> orderProductInfoDTO = getOrderDataDTOList(optionIdAndCountDTO);

        if(orderProductInfoDTO.isEmpty() || optionIdAndCountDTO.size() != orderProductInfoDTO.size())
            throw new IllegalArgumentException("product Order Data is empty");

        OrderDataResponseDTO responseDTO = orderDomainService.mappingOrderResponseDTO(optionIdAndCountDTO, orderProductInfoDTO);
        if(userId == null)
            userId = Role.ANONYMOUS.getRole();

        saveOrderValidateData(orderTokenCookie, response, responseDTO, userId);

        return responseDTO;
    }

    /**
     *
     * @param cartDetailIds
     * @param cartCookie
     * @param userId
     * @param response
     *
     * 장바구니에서 주문 요청시 상품 정보 조회.
     * 상품 정보 및 가격, 주문 토큰 생성 및 반환
     */
    public OrderDataResponseDTO getCartOrderData(List<Long> cartDetailIds,
                                                 Cookie orderTokenCookie,
                                                 Cookie cartCookie,
                                                 String userId,
                                                 HttpServletResponse response) {
        CartMemberDTO cartMemberDTO = cartDomainService.setCartMemberDTO(cartCookie, userId);

        List<CartDetail> cartDetails = cartDataService.getCartDetailListByIds(cartDetailIds);

        if(cartDetails.isEmpty())
            throw new CustomNotFoundException(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage());

        Long cartId = cartDetails.get(0).getCart().getId();
        Cart cart = cartDataService.getCartByIdOrElseIllegal(cartId);

        if(!cart.getMember().getUserId().equals(cartMemberDTO.uid())
                || !Objects.equals(cart.getCookieId(), cartMemberDTO.cartCookieValue()))
            throw new CustomAccessDeniedException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage());

        List<OrderProductRequestDTO> optionIdAndCountDTO = cartDetails.stream()
                                                                    .map(dto ->
                                                                            new OrderProductRequestDTO(
                                                                                    dto.getProductOption().getId()
                                                                                    , dto.getCartCount()
                                                                            )
                                                                    )
                                                                    .toList();

        List<OrderProductInfoDTO> orderProductInfoDTO = getOrderDataDTOList(optionIdAndCountDTO);
        OrderDataResponseDTO responseDTO = orderDomainService.mappingOrderResponseDTO(optionIdAndCountDTO, orderProductInfoDTO);
        saveOrderValidateData(orderTokenCookie, response, responseDTO, cartMemberDTO.uid());

        return responseDTO;
    }

    private List<OrderProductInfoDTO> getOrderDataDTOList(List<OrderProductRequestDTO> optionIdAndCountDTO) {
        List<Long> optionIds = optionIdAndCountDTO.stream().map(OrderProductRequestDTO::optionId).toList();

        return productDataService.getOrderInfoDTOListByOptionIds(optionIds);
    }

    private void saveOrderValidateData(Cookie orderTokenCookie, HttpServletResponse response, OrderDataResponseDTO requestDTO, String userId) {
        if(orderTokenCookie != null)
            orderDataService.deleteOrderTokenData(orderTokenCookie.getValue());

        String token = orderCookieWriter.createAndSetOrderTokenCookie(response);
        List<OrderItemVO> orderItemVOList = requestDTO.orderData().stream().map(OrderDataDTO::toOrderItemVO).toList();
        PreOrderDataVO preOrderDataVO = new PreOrderDataVO(userId, orderItemVOList, requestDTO.totalPrice());
        orderDataService.setOrderValidateDataToRedis(token, preOrderDataVO);
    }
}
