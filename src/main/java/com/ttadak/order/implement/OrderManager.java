package com.ttadak.order.implement;

import com.ttadak.order.domain.Order;
import com.ttadak.order.domain.Receiver;
import com.ttadak.order.infrastructure.OrderRepository;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderManager {

    private final OrderRepository orderRepository;

    public Order create(
        Long productId,
        int unitPrice,
        int quantity,
        String receiverName,
        String receiverPhone,
        String receiverAddress
    ) {
        Order order = Order.place(
            productId,
            unitPrice,
            quantity,
            new Receiver(receiverName, receiverPhone, receiverAddress),
            LocalDateTime.now()
        );

        Order saved = orderRepository.save(order);
        log.info("[OrderManager] 주문 - orderId={}, productId={}", saved.getId(), productId);

        return saved;
    }

    @Transactional
    public Order cancel(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        order.cancel(LocalDateTime.now());
        return order;
    }

}
