package com.ttadak.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.order.fixture.OrderFixture;
import com.ttadak.shared.Covers;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 1, 9, 0);

    @Covers("R-1.5")
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void 주문_수량이_1_이상이면_주문된다(int quantity) {
        Order order = OrderFixture.order().quantity(quantity).place();

        assertThat(order.getQuantity()).isEqualTo(quantity);
    }

    @Covers("R-1.5")
    @Test
    void 주문_수량이_0이면_주문이_거절된다() {
        assertThatThrownBy(() -> OrderFixture.order().quantity(0).place())
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

    @Covers("R-1.8")
    @Test
    void 주문하면_주문_금액은_가격에_수량을_곱한_값이다() {
        Order order = OrderFixture.order().unitPrice(1000).quantity(3).place();

        assertThat(order.getTotalPrice()).isEqualTo(3000);
    }

    @Covers("R-1.1")
    @Test
    void 단가가_음수이면_주문이_거절된다() {
        assertThatThrownBy(() -> OrderFixture.order().unitPrice(-1).place())
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

    @Covers("AC-1.4")
    @Test
    void 주문하면_주문됨_상태로_주문_시각이_남는다() {
        Order order = OrderFixture.order().orderedAt(NOW).place();

        assertThat(order.getState()).isEqualTo(OrderState.ORDERED);
        assertThat(order.getOrderedAt()).isEqualTo(NOW);
    }

    @Covers("AC-1.8")
    @Test
    void 주문을_취소하면_취소됨_상태로_취소_시각이_남는다() {
        Order order = OrderFixture.order().place();

        order.cancel(NOW);

        assertThat(order.isCanceled()).isTrue();
        assertThat(order.getCanceledAt()).isEqualTo(NOW);
    }

    @Covers("R-1.10")
    @Test
    void 이미_취소한_주문을_취소하면_거절된다() {
        Order order = OrderFixture.order().place();
        order.cancel(NOW);

        assertThatThrownBy(() -> order.cancel(NOW))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_CANCELED);
    }

    @Covers("R-1.15")
    @Test
    void 받는_사람이_없으면_주문이_거절된다() {
        assertThatThrownBy(() -> Order.place(1L, 1000, 1, null, NOW))
            .isInstanceOf(NullPointerException.class);
    }

}
