package com.ttadak.order.domain;

import static java.util.Objects.requireNonNull;

import com.ttadak.shared.entity.BaseEntity;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

    @Column(nullable = false)
    private Long productId;

    private int quantity;

    @Column(name = "total_price")
    private int totalPrice;

    @Embedded
    private Receiver receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    private LocalDateTime canceledAt;

    public static Order place(
        Long productId,
        int unitPrice,
        int quantity,
        Receiver receiver,
        LocalDateTime now
    ) {
        validate(unitPrice, quantity);

        Order order = new Order();

        order.productId = requireNonNull(productId, "productId");
        order.quantity = quantity;
        order.totalPrice = unitPrice * quantity;
        order.receiver = requireNonNull(receiver, "receiver");
        order.state = OrderState.ORDERED;
        order.orderedAt = now;

        return order;
    }

    public void cancel(LocalDateTime now) {
        if (isCanceled()) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELED);
        }
        state = OrderState.CANCELED;
        canceledAt = now;
    }

    public boolean isCanceled() {
        return state == OrderState.CANCELED;
    }

    private static void validate(int unitPrice, int quantity) {
        if (unitPrice < 0) {
            throw CustomException.validation("unitPrice", "단가는 0 이상이어야 합니다.");
        }
        if (quantity < 1) {
            throw CustomException.validation("quantity", "주문 수량은 1 이상이어야 합니다.");
        }
    }

}
