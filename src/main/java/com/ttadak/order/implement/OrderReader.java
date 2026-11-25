package com.ttadak.order.implement;

import com.ttadak.order.domain.Order;
import com.ttadak.order.infrastructure.OrderRepository;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderReader {

    private final OrderRepository orderRepository;

    public Order find(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

}
