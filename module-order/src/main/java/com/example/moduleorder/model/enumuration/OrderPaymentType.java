package com.example.moduleorder.model.enumuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum OrderPaymentType {
    CARD("card"),
    CASH("cash");

    private final String type;

    public static void validate(String paymentType) {
        boolean exists = Arrays.stream(values())
                .anyMatch(v -> v.type.equalsIgnoreCase(paymentType));

        if(!exists)
            throw new IllegalArgumentException("Invalid OrderPaymentType : " + paymentType);
    }
}
