package com.example.modulecart.service;

import com.example.modulecart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartStore {

    private final CartRepository cartRepository;

    public void deleteById(long cartId) {
        cartRepository.deleteById(cartId);
    }

    public void deleteAllByIds(List<Long> cartIds) {
        cartRepository.deleteAllById(cartIds);
    }
}
