package com.example.moduleorder.usecase.admin;

import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderExternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminOrderWriteUseCaseUnitTest {

    @InjectMocks
    private AdminOrderWriteUseCase adminOrderWriteUseCase;

    @Mock
    private OrderDataService orderDataService;

    @Mock
    private OrderExternalService orderExternalService;

    private List<ProductOrder> getOrderFixtureList() {
        List<Member> memberFixtureList = MemberAndAuthFixture.createDefaultMember(30).memberList();
        List<ProductOption> productOptionFixtureList = ProductFixture.createDefaultProductByOUTER(20)
                .stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();

        return ProductOrderFixture.createSaveProductOrder(memberFixtureList, productOptionFixtureList);
    }

    @Test
    @DisplayName(value = "주문 데이터 상태 갱신")
    void orderPreparation() {
        ProductOrder productOrderFixture = getOrderFixtureList().get(0);

        when(orderDataService.findProductOrderByIdOrElseIllegal(anyLong())).thenReturn(productOrderFixture);
        doNothing().when(orderDataService).saveProductOrder(any(ProductOrder.class));
        doNothing().when(orderExternalService).sendOrderNotification(any());

        assertDoesNotThrow(() -> adminOrderWriteUseCase.orderPreparation(1L));
    }

    @Test
    @DisplayName(value = "주문 데이터 상태 갱신. 주문 아이디가 잘못된 경우")
    void orderPreparationWrongId() {

        when(orderDataService.findProductOrderByIdOrElseIllegal(anyLong())).thenThrow(IllegalArgumentException.class);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminOrderWriteUseCase.orderPreparation(1L)
        );

        verify(orderDataService, never()).saveProductOrder(any(ProductOrder.class));
        verify(orderExternalService, never()).sendOrderNotification(any());
    }
}
