package com.ttadak.order.application;

import com.ttadak.order.application.dto.NewOrderItem;
import com.ttadak.order.application.dto.NewOrderReceiver;
import com.ttadak.order.domain.Order;
import com.ttadak.order.implement.OrderManager;
import com.ttadak.order.implement.OrderReader;
import com.ttadak.product.domain.Product;
import com.ttadak.product.implement.ProductManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final ProductManager productManager;
    private final OrderReader orderReader;
    private final OrderManager orderManager;

    @Transactional
    public Order create(NewOrderItem item, NewOrderReceiver receiver, int memberId) {
        // 회원 조회 했다 가정.

        Product product = productManager.decreaseStock(item.productId(), item.quantity());

        return orderManager.create(
            product.getId(),
            product.getPrice().amount(),
            item.quantity(),
            receiver.name(),
            receiver.phone(),
            receiver.address()
        );
    }

    @Transactional
    public void cancel(Long orderId) {
        Order cancel = orderManager.cancel(orderId);
        productManager.increaseStock(cancel.getProductId(), cancel.getQuantity());

        log.info("[OrderService] 주문 취소 - orderId={}", orderId);
    }

    @Transactional(readOnly = true)
    public Order findOrder(Long orderId) {
        return orderReader.find(orderId);
    }

}
