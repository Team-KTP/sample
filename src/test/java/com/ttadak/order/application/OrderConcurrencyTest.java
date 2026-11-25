package com.ttadak.order.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.ttadak.order.application.dto.NewOrderItem;
import com.ttadak.order.fixture.OrderFixture;
import com.ttadak.product.domain.Product;
import com.ttadak.product.fixture.ProductFixture;
import com.ttadak.product.infrastructure.ProductRepository;
import com.ttadak.shared.Covers;
import com.ttadak.shared.IntegrationTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

@IntegrationTest
class OrderConcurrencyTest {

    private static final int STOCK = 5;
    private static final int ORDER_COUNT = 10;

    private final OrderService orderService;
    private final ProductRepository productRepository;

    OrderConcurrencyTest(OrderService orderService, ProductRepository productRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    @Covers({"R-1.11", "AC-1.11"})
    @Test
    void 재고보다_많은_주문이_동시에_들어오면_재고만큼만_주문이_성공한다() throws InterruptedException {
        Product product = productRepository.save(ProductFixture.product().stock(STOCK).create());
        var item = new NewOrderItem(product.getId(), 1);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(ORDER_COUNT);
        var succeeded = new AtomicInteger();

        try (ExecutorService executor = Executors.newFixedThreadPool(ORDER_COUNT)) {
            for (int i = 0; i < ORDER_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        start.await();
                        orderService.create(item, OrderFixture.receiver(), 1);
                        succeeded.incrementAndGet();
                    } catch (Exception ignored) {
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            done.await(10, TimeUnit.SECONDS);
        }

        assertThat(succeeded.get()).isEqualTo(STOCK);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isZero();
    }

}
