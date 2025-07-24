package com.example.modulecart.service;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartDomainService {

    private final ProductOptionRepository productOptionRepository;

    public CartMemberDTO setCartMemberDTO(Cookie cookie, String userId) {
        /**
         * 둘다 null이라면
         * Cookie를 생성한다.
         *
         * 아니라면 둘을 DTO에 담는다.
         * 단, userId가 null이라면 Role.ANONYMOUS를 담아준다.
         */

        String uid = userId;
        String cookieValue = null;

        if(userId == null) {
            cookieValue = cookie == null ? createAnonymousCartCookie() : cookie.getValue();
            uid = Role.ANONYMOUS.getRole();
        }

        return new CartMemberDTO(uid, cookieValue);
    }

    private String createAnonymousCartCookie() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Cart buildCart(Member member, String cookieValue) {
        return Cart.builder()
                .member(member)
                .cookieId(cookieValue)
                .build();
    }

    public List<Long> mapAddCartOptionIds(List<AddCartDTO> addList) {
        return addList.stream()
                .map(AddCartDTO::optionId)
                .toList();
    }

    public void mapCartAndCartDetails(List<AddCartDTO> addList, Cart userCart, List<CartDetail> savedDetails) {

        for(AddCartDTO addCartDTO : addList) {
            CartDetail addDetailEntity = CartDetail.builder()
                    .productOption(
                            productOptionRepository.findById(addCartDTO.optionId())
                                    .orElseThrow(IllegalArgumentException::new)
                    )
                    .cartCount(addCartDTO.count())
                    .build();

            if(!savedDetails.isEmpty()){
                CartDetail existsDetailEntity = findDetailAndPatchCount(addCartDTO, savedDetails);
                addDetailEntity = existsDetailEntity == null ? addDetailEntity : existsDetailEntity;
            }

            userCart.addCartDetail(addDetailEntity);
        }
    }

    public CartDetail findDetailAndPatchCount(AddCartDTO addCartDTO, List<CartDetail> savedDetails) {
        CartDetail result = null;

        for(int i = 0; i < savedDetails.size(); i++) {
            CartDetail listObject = savedDetails.get(i);
            if(addCartDTO.optionId().equals(listObject.getProductOption().getId())){
                listObject.addCartCount(addCartDTO.count());

                result = listObject;
                savedDetails.remove(i);
                break;
            }
        }

        return result;
    }

    public void validateDeleteIdsFromUserDetailIds(List<Long> deleteSelectIds, List<Long> userCartDetailIds) {
        for(Long deleteSelectId : deleteSelectIds)
            if(!userCartDetailIds.contains(deleteSelectId))
                throw new IllegalArgumentException("Invalid CartDetailId");
    }
}
