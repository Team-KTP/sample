package com.ttadak.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.order.application.dto.NewOrderItem;
import com.ttadak.order.application.dto.NewOrderReceiver;
import com.ttadak.order.domain.Order;
import com.ttadak.order.domain.OrderState;
import com.ttadak.order.fixture.OrderFixture;
import com.ttadak.order.infrastructure.OrderRepository;
import com.ttadak.product.domain.Product;
import com.ttadak.product.fixture.ProductFixture;
import com.ttadak.product.infrastructure.ProductRepository;
import com.ttadak.shared.Covers;
import com.ttadak.shared.IntegrationTest;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import org.junit.jupiter.api.Test;

@IntegrationTest
class OrderServiceTest {

    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    OrderServiceTest(
        OrderService orderService,
        ProductRepository productRepository,
        OrderRepository orderRepository
    ) {
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Covers({"AC-1.4", "R-1.7"})
    @Test
    void 주문하면_줄어든_재고가_저장된다() {
        Product product = productRepository.save(ProductFixture.product().stock(5).create());

        orderService.create(item(product.getId(), 2), OrderFixture.receiver(), 1);

        assertThat(stockOf(product.getId())).isEqualTo(3);
    }

    @Covers({"AC-1.5", "R-1.6"})
    @Test
    void 재고보다_많이_주문하면_주문도_재고도_바뀌지_않는다() {
        Product product = productRepository.save(ProductFixture.product().stock(1).create());
        var item = item(product.getId(), 2);

        assertThatThrownBy(() -> orderService.create(item, OrderFixture.receiver(), 1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OUT_OF_STOCK);

        assertThat(orderRepository.count()).isZero();
        assertThat(stockOf(product.getId())).isEqualTo(1);
    }

    @Covers({"AC-1.6", "R-1.15"})
    @Test
    void 재고를_줄인_뒤에_주문이_거절되면_재고가_되돌아온다() {
        Product product = productRepository.save(ProductFixture.product().stock(5).create());
        var item = item(product.getId(), 2);
        var receiver = new NewOrderReceiver(" ", "010-0000-0000", "서울시 어딘가");

        assertThatThrownBy(() -> orderService.create(item, receiver, 1))
            .isInstanceOf(CustomException.class);

        assertThat(orderRepository.count()).isZero();
        assertThat(stockOf(product.getId())).isEqualTo(5);
    }

    @Covers("AC-1.10")
    @Test
    void 없는_상품을_주문하면_상품_없음으로_거절된다() {
        var item = item(999L, 1);

        assertThatThrownBy(() -> orderService.create(item, OrderFixture.receiver(), 1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Covers("AC-1.7")
    @Test
    void 주문을_조회하면_주문한_상품의_id가_함께_읽힌다() {
        Product product = productRepository.save(ProductFixture.product().create());
        Order placed = orderService
            .create(item(product.getId(), 1), OrderFixture.receiver(), 1);

        Order found = orderService.findOrder(placed.getId());

        assertThat(found.getProductId()).isEqualTo(product.getId());
    }

    @Covers("AC-1.7")
    @Test
    void 없는_주문을_조회하면_주문_없음으로_거절된다() {
        assertThatThrownBy(() -> orderService.findOrder(999L))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Covers({"AC-1.8", "R-1.9"})
    @Test
    void 주문을_취소하면_취소_상태와_돌아온_재고가_저장된다() {
        Product product = productRepository.save(ProductFixture.product().stock(5).create());
        Order placed = orderService
            .create(item(product.getId(), 2), OrderFixture.receiver(), 1);

        orderService.cancel(placed.getId());

        assertThat(orderRepository.findById(placed.getId()).orElseThrow().getState())
            .isEqualTo(OrderState.CANCELED);
        assertThat(stockOf(product.getId())).isEqualTo(5);
    }

    private NewOrderItem item(Long productId, int quantity) {
        return new NewOrderItem(productId, quantity);
    }

    private int stockOf(Long productId) {
        return productRepository.findById(productId).orElseThrow().getStock();
    }

}
