package com.example.modulecart.repository;

import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecommon.model.entity.CartDetail;

import java.util.List;

public interface CartDetailDSLRepository {

    List<CartDetailDTO> findAllByCartId(long cartId);

    List<CartDetail> findAllCartDetailByCartIdAndOptionIds(long cartId, List<Long> optionIds);

    List<Long> findAllIdByCartId(long cartId);
}
