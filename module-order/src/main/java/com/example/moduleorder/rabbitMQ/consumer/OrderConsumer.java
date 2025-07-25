package com.example.moduleorder.rabbitMQ.consumer;

import com.example.modulecart.service.CartReader;
import com.example.modulecart.service.CartStore;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderCartDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductMessageDTO;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.model.dto.product.business.PatchOrderStockDTO;
import com.example.moduleproduct.service.product.ProductOptionStore;
import com.example.moduleproduct.service.product.ProductStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OrderConsumer {
    /**
     * 매출 메시지 2개는 module-admin의 OrderSalesConsumer로 분리
     * module-admin이 module-order를 참조하고 있기 때문에 관련 Repository 사용을 위함.
     *
     * admin 기능 중 재처리 메시지의 경우 여기에 배치.
     * ProductOrderRepository의 위치가 여기이기도 하고
     * 주문 데이터 처리이기 때문에 요청은 admin으로 전달되더라도 여기에 두는게 맞다고 생각.
     */

    private final ProductStore productStore;

    private final ProductOptionStore productOptionStore;

    private final CartReader cartReader;

    private final CartStore cartStore;

    private final ProductOrderRepository productOrderRepository;

    public OrderConsumer(ProductStore productStore,
                         ProductOptionStore productOptionStore,
                         CartReader cartReader,
                         CartStore cartStore,
                         ProductOrderRepository productOrderRepository) {
        this.productStore = productStore;
        this.productOptionStore = productOptionStore;
        this.cartReader = cartReader;
        this.cartStore = cartStore;
        this.productOrderRepository = productOrderRepository;
    }

    /**
     *
     * @param messageDTO
     *
     * 상품 주문 처리 시 Product의 productSalesQuantity 데이터 수정.
     * 동일한 상품이더라도 옵션이 다른 경우 각 OrderProductDTO에 담겨 있기 때문에 Map을 통해 전체 집계 처리.
     * 수정 처리는 조회로 인해 발생하는 시간을 줄이기 위해 save() 처리를 지양.
     * Map 구조 그대로 productId, salesQuantity 구조 그대로 Repository로 보내고 update 쿼리를 통해 수정하도록 처리.
     *
     * 또한 동시성 제어를 위해 concurrency 1로 설정.
     */
    @RabbitListener(queues = "${rabbitmq.queue.orderProduct.name}", concurrency = "1")
    public void consumeOrderProduct(OrderProductMessageDTO messageDTO) {
        Map<String, Integer> productMap = new HashMap<>();

        for(OrderProductDTO dto : messageDTO.getOrderProductList()){
            productMap.put(
                    dto.getProductId(),
                    productMap.getOrDefault(dto.getProductId(), 0) + dto.getDetailCount()
            );
        }

        productStore.patchProductSalesQuantity(productMap);
    }

    /**
     *
     * @param messageDTO
     *
     * 상품 옵션별 재고 수정.
     * OrderProductDTO 내부의 optionId, detailCount 필드를 통해 update 쿼리로 직접 수정.
     * 이 처리는 처리시간을 최대한 줄여야 상품 관련 UI에서 품절 여부를 빠르게 표시할 수 있기 때문에 save()가 아닌 update 쿼리로 처리.
     *
     * 동시성 제어를 위해 concurrency 1 로 설정.
     */
    @RabbitListener(queues = "${rabbitmq.queue.orderProductOption.name}", concurrency = "1")
    public void consumeOrderProductOption(OrderProductMessageDTO messageDTO) {
        List<PatchOrderStockDTO> patchDTO = messageDTO.getOrderProductList()
                                                .stream()
                                                .map(OrderProductDTO::toPatchOrderStockDTO)
                                                .toList();
        productOptionStore.patchOrderStock(patchDTO);
    }

    /**
     *
     * @param orderCartDTO
     *
     * 주문 데이터에 해당하는 장바구니 데이터 삭제 처리.
     * 장바구니를 우선 조회하고 해당 데이터 사이즈와 주문 데이터 사이즈가 동일하다면 전체 주문으로 판단하고 장바구니 자체를 삭제.
     * 크기가 다르다면 일치하는 데이터만 찾아서 제거한다.
     *
     * cartId 조회 시 null이 반환된다면 NullPointerException 발생으로 인해 재시도 및 DLQ로 이동하지 않는 문제가 발생하기 때문에
     * null인 경우 로그를 남기도록 처리. 그로 인해 NullPointerException으로 인한 재시도나 큐에 메시지가 잔류하는 것을 막음.
     *
     * 장바구니 데이터 삭제가 목적이기 때문에 null이더라도 처리에 대한 문제가 발생하지는 않을 것으로 생각.
     * 대신 "cart" 로 인해 장바구니를 통한 주문 요청이었는데 Null이 된다는 것은 잘못된 요청이기 떄문에 이로 인한 검증이나 대응책을 고려해서
     * 서비스 메소드 상에서 추가적인 처리가 필요할 것 같음.
     */
    @RabbitListener(queues = "${rabbitmq.queue.orderCart.name}", concurrency = "3")
    public void consumeOrderCart(OrderCartDTO orderCartDTO) {
        Long cartId = cartReader.findIdByUserId(orderCartDTO.getCartMemberDTO());
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!cart consumer userId : " + orderCartDTO.getCartMemberDTO());
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!cart consumer cartId : " + cartId);

        List<Cart> testCart = cartReader.findAll();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!cart consumer testCart size : " + testCart.size());
//        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!cart consumer cartId : " + cartId);

        if(cartId != null){
            List<CartDetail> cartDetailList = cartReader.findAllCartDetailByCartId(cartId);

            if(cartDetailList.size() == orderCartDTO.getProductOptionIds().size())
                cartStore.deleteById(cartId);
            else {
                List<Long> deleteCartDetailIds = cartDetailList.stream()
                        .filter(cartDetail ->
                                orderCartDTO.getProductOptionIds().contains(
                                        cartDetail.getProductOption().getId()
                                )
                        )
                        .map(CartDetail::getId)
                        .toList();

                cartStore.deleteAllByIds(deleteCartDetailIds);
            }
        }else
            log.error("OrderConsumer::consumeOrderCart : cartId is null. cartMemberDTO is {}", orderCartDTO.getCartMemberDTO());
    }

    @RabbitListener(queues = "${rabbitmq.queue.failedOrder.name}", concurrency = "3")
    public void consumeFailedOrderData(ProductOrderDataDTO productOrderDataDTO) {
        ProductOrder order = productOrderDataDTO.productOrder();
        productOrderRepository.save(order);
    }
}
