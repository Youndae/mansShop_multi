package com.example.moduleorder.model.dto.rabbitMQ;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderCartDTO {
    private CartMemberDTO cartMemberDTO;
    private List<Long> productOptionIds;
}
