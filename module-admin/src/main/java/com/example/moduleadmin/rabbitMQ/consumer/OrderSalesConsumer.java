package com.example.moduleadmin.rabbitMQ.consumer;

import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.modulecommon.model.entity.*;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductSummaryDTO;
import com.example.moduleorder.model.dto.rabbitMQ.PeriodSummaryQueueDTO;
import com.example.moduleproduct.model.dto.product.business.ProductIdClassificationDTO;
import com.example.moduleproduct.service.product.ProductDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class OrderSalesConsumer {
    /**
     * 결제 완료 이후 주문 데이터 처리 과정 중 매출 관련 2개의 메시지 처리.
     * module-admin이 module-order를 참조하고 있기 때문에 Repository를 위해 분리
     */

    private final PeriodSalesSummaryRepository periodSalesSummaryRepository;

    private final ProductSalesSummaryRepository productSalesSummaryRepository;

    private final ProductDataService productDataService;

    public OrderSalesConsumer(PeriodSalesSummaryRepository periodSalesSummaryRepository,
                              ProductSalesSummaryRepository productSalesSummaryRepository,
                              ProductDataService productDataService) {
        this.periodSalesSummaryRepository = periodSalesSummaryRepository;
        this.productSalesSummaryRepository = productSalesSummaryRepository;
        this.productDataService = productDataService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.periodSalesSummary.name}", concurrency = "1")
    public void consumePeriodSalesSummary(PeriodSummaryQueueDTO dto) {
        PeriodSalesSummary entity = periodSalesSummaryRepository.findByPeriod(dto.getPeriod());

        if(entity != null){
            PeriodSalesSummary patchDummyEntity = dto.toEntity();
            entity.setPatchData(patchDummyEntity);
        } else
            entity = dto.toEntity();

        periodSalesSummaryRepository.save(entity);
    }

    /**
     *
     * @param productSummaryDTO
     *
     * 상품 매출 테이블 수정 처리.
     * 상품 매출 데이터의 경우 해당 날짜에서는 상품 옵션이 Unique해야 하기 때문에 조회 후 수정 또는 삽입 처리로 수행.
     * 날짜 필드의 경우 Queue 대기 시간을 고려해서 서비스 로직에서 미리 생성해서 전달.
     *
     * 처리 분기
     * 1. 조회 결과와 요청 데이터의 크기가 동일하다면 해당 날짜의 모든 상품 매출 데이터가 존재하므로 조회된 엔티티 리스트를 수정.
     * 2. 조회 결과가 0으로 나온다면 모든 데이터가 존재하지 않는 것으로 요청 데이터를 통해 모든 엔티티를 생성.
     * 3. 조회 결과가 0은 아니지만 요청 데이터의 크기와 다르다면 기존 데이터를 수정하고 남은 데이터를 통해 엔티티 생성 후 리스트에 추가.
     *
     * 동시성 제어를 위해 concurrency 1 로 처리.
     */
    @RabbitListener(queues = "${rabbitmq.queue.productSalesSummary.name}", concurrency = "1")
    public void consumeProductSalesSummary(OrderProductSummaryDTO productSummaryDTO) {
        List<ProductSalesSummary> summaryEntities = productSalesSummaryRepository.findAllByProductOptionIds(productSummaryDTO.getPeriodMonth(), productSummaryDTO.getProductOptionIds());
        if(summaryEntities.size() == productSummaryDTO.getOrderProductDTOList().size()) {
            //둘의 사이즈가 같다는 것은 해당 상품에 대한 데이터가 이미 들어가 있다는 말이 되므로 기존 엔티티 수정만 처리.
            patchProductSalesSummaryList(summaryEntities, productSummaryDTO.getOrderProductDTOList());
        }else if (summaryEntities.isEmpty()){
            createProductSummaryList(summaryEntities, productSummaryDTO.getOrderProductDTOList(), productSummaryDTO.getProductIds(), productSummaryDTO.getPeriodMonth());
        }else {
            //기존 엔티티 수정 처리 후 새로운 데이터에 대한 삽입을 처리하기 위해 productIds를 만든다.
            // 이때, 엔티티에 없는 정보만 조회해야 한다.
            patchProductSalesSummaryList(summaryEntities, productSummaryDTO.getOrderProductDTOList());

            List<String> productIds = new ArrayList<>();
            List<OrderProductDTO> remainDTO = new ArrayList<>();

            // 기존 데이터가 존재하지 않더라도 해당 옵션에 대해 ProductId, ClassificationId가 동일한 상품이라면 DTO에서 데이터를 찾아 파싱할 필요가 없어지므로
            // 해당 처리에 대한 체크를 수행.
            for(OrderProductDTO dto : productSummaryDTO.getOrderProductDTOList()) {
                ProductSalesSummary newEntity = null;
                for(ProductSalesSummary entity : summaryEntities) {

                    if(dto.getOptionId() != entity.getProductOption().getId() &&
                            dto.getProductId().equals(entity.getProduct().getId())){
                        // 옵션 아이디는 불일치하지만 상품 아이디는 일치한다면
                        // 이 Product, Classification을 그대로 담아서 사용할 수 있다.

                        newEntity = ProductSalesSummary.builder()
                                .periodMonth(productSummaryDTO.getPeriodMonth())
                                .classification(entity.getClassification())
                                .product(entity.getProduct())
                                .productOption(ProductOption.builder().id(dto.getOptionId()).build())
                                .sales(dto.getDetailPrice())
                                .salesQuantity(dto.getDetailCount())
                                .orderQuantity(1)
                                .build();

                        summaryEntities.add(newEntity);
                        break;
                    }
                }
                // newEntity가 그대로 null이라면 일치하는 상품이 없었다는 것이므로 상품 아이디와 OrderProductDTO를 리스트에 추가.
                if(newEntity == null) {
                    productIds.add(dto.getProductId());
                    remainDTO.add(dto);
                }
            }
            // 기존 데이터와 비교 후 일치하는 데이터가 없는 요청 데이터에 대해 엔티티를 생성하고 리스트에 추가하기 위해 호출.
            if(!productIds.isEmpty())
                createProductSummaryList(summaryEntities, remainDTO, productIds, productSummaryDTO.getPeriodMonth());
        }
        productSalesSummaryRepository.saveAll(summaryEntities);
    }

    /**
     *
     * @param list
     * @param requestDTO
     *
     * 상품 매출 테이블의 기존 데이터 수정 처리 메소드.
     * 수정이 처리된 요청 데이터는 더이상 필요하지 않기도 하고 처리되지 않은 데이터를 상위 메소드에서 확인하도록 하기 위해 remove() 처리.
     */
    private void patchProductSalesSummaryList(List<ProductSalesSummary> list,
                                              List<OrderProductDTO> requestDTO) {
        for(ProductSalesSummary entity : list) {

            for(OrderProductDTO dto : requestDTO) {
                if(dto.getOptionId() == entity.getProductOption().getId()){
                    entity.setPatchSalesData(dto.getDetailPrice(), dto.getDetailCount());
                    requestDTO.remove(dto);
                    break;
                }
            }
        }
    }

    /**
     *
     * @param list
     * @param requestDTO
     * @param productIds
     * @param periodMonth
     *
     * 상품 매출 테이블에 존재하지 않는 데이터들에 대해 엔티티를 생성하고 리스트에 추가.
     */
    private void createProductSummaryList(List<ProductSalesSummary> list,
                                          List<OrderProductDTO> requestDTO,
                                          List<String> productIds,
                                          LocalDate periodMonth) {
        List<ProductIdClassificationDTO> searchDTO = productDataService.getClassificationIdAndProductIdByProductIds(productIds);
        for(OrderProductDTO dto : requestDTO) {
            String productId = dto.getProductId();

            for(ProductIdClassificationDTO searchData : searchDTO) {
                if(productId.equals(searchData.productId())) {
                    list.add(
                            ProductSalesSummary.builder()
                                    .periodMonth(periodMonth)
                                    .classification(Classification.builder().id(searchData.classificationId()).build())
                                    .product(Product.builder().id(searchData.productId()).build())
                                    .productOption(ProductOption.builder().id(dto.getOptionId()).build())
                                    .sales(dto.getDetailPrice())
                                    .salesQuantity(dto.getDetailCount())
                                    .orderQuantity(1L)
                                    .build()
                    );
                }
            }
        }
    }
}
