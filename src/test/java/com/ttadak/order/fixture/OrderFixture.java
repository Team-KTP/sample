package com.ttadak.order.fixture;

import com.ttadak.order.application.dto.NewOrderReceiver;
import com.ttadak.order.domain.Order;
import com.ttadak.order.domain.Receiver;
import java.time.LocalDateTime;

public final class OrderFixture {

    public static final LocalDateTime ORDERED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    private Long productId = 1L;
    private int unitPrice = 1000;
    private int quantity = 1;
    private Receiver receiver = new Receiver("홍길동", "010-0000-0000", "서울시 어딘가");
    private LocalDateTime orderedAt = ORDERED_AT;

    private OrderFixture() {
    }

    public static OrderFixture order() {
        return new OrderFixture();
    }

    public static NewOrderReceiver receiver() {
        return new NewOrderReceiver("홍길동", "010-0000-0000", "서울시 어딘가");
    }

    public OrderFixture productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public OrderFixture unitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public OrderFixture quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderFixture orderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
        return this;
    }

    public Order place() {
        return Order.place(productId, unitPrice, quantity, receiver, orderedAt);
    }

}
