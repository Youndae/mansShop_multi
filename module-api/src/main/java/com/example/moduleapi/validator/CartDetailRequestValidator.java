package com.example.moduleapi.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CartDetailRequestValidator {

    public void validateCartDetailIds(List<Long> cartDetailIds) {
        if(cartDetailIds == null || cartDetailIds.isEmpty()){
            log.warn("CartDetailRequestValidator.validateCartDetailIds :: cartDetailIds is Null or is Empty");
            throw new IllegalArgumentException("cartDetailIds is null or empty");
        }else {
            for(Long detailId : cartDetailIds) {
                if(detailId == null || detailId < 1L){
                    log.warn("CartDetailRequestValidator.validateCartDetailIds :: cartDetailId is Null. cartDetailIds = {}", cartDetailIds);
                    throw new IllegalArgumentException("cartDetailId is Null");
                }
            }
        }
    }
}
