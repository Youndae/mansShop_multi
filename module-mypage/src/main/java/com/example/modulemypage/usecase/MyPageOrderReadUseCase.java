package com.example.modulemypage.usecase;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulemypage.model.dto.out.MyPageOrderDTO;
import com.example.modulemypage.service.MyPageOrderService;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageOrderReadUseCase {

    private final MyPageOrderService mypageOrderService;

    public PagingListDTO<MyPageOrderDTO> getOrderList(OrderPageDTO orderPageDTO, MemberOrderDTO memberOrderDTO) {

        return mypageOrderService.getOrderList(orderPageDTO, memberOrderDTO);
    }
}
