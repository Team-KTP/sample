package com.ttadak.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.product.fixture.ProductFixture;
import com.ttadak.shared.Covers;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    @Covers("R-1.2")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 이름이_비어_있으면_상품_등록이_거절된다(String name) {
        assertThatThrownBy(() -> register(name, "설명", 1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

    @Covers("R-1.3")
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void 재고가_0_이상이면_상품이_등록된다(int stock) {
        Product product = register("머그컵", "설명", stock);

        assertThat(product.getStock()).isEqualTo(stock);
    }

    @Covers("R-1.3")
    @Test
    void 재고가_음수이면_상품_등록이_거절된다() {
        assertThatThrownBy(() -> register("머그컵", "설명", -1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

    @Covers("R-1.12")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 설명이_비어_있으면_상품_등록이_거절된다(String description) {
        assertThatThrownBy(() -> register("머그컵", description, 1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

    @Test
    void 가격이_없으면_상품_등록이_거절된다() {
        var shippingPolicy = new ShippingPolicy(3000, 1);

        assertThatThrownBy(() -> Product.register("머그컵", "설명", 1, null, shippingPolicy))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 배송_정책이_없으면_상품_등록이_거절된다() {
        var price = new Price(1000);

        assertThatThrownBy(() -> Product.register("머그컵", "설명", 1, price, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Covers("R-1.4")
    @ParameterizedTest
    @CsvSource({"0, true", "1, false"})
    void 재고가_0이면_품절이다(int stock, boolean soldOut) {
        Product product = ProductFixture.product().stock(stock).create();

        assertThat(product.isSoldOut()).isEqualTo(soldOut);
    }

    @Covers({"R-1.6", "R-1.7"})
    @ParameterizedTest
    @CsvSource({"2, 1", "3, 0"})
    void 재고_이하로_주문하면_재고가_주문_수량만큼_준다(int quantity, int remaining) {
        Product product = ProductFixture.product().stock(3).create();

        product.decreaseStock(quantity);

        assertThat(product.getStock()).isEqualTo(remaining);
    }

    @Covers("R-1.6")
    @Test
    void 재고보다_많이_주문하면_재고_부족으로_거절된다() {
        Product product = ProductFixture.product().stock(3).create();

        assertThatThrownBy(() -> product.decreaseStock(4))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OUT_OF_STOCK);
    }

    @Covers("R-1.1")
    @Test
    void 가격을_수정하면_바뀐_가격이_된다() {
        Product product = ProductFixture.product().price(1000).create();

        product.modifyPrice(new Price(1500));

        assertThat(product.getPrice()).isEqualTo(new Price(1500));
    }

    @Covers("R-1.5")
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 수량이_1보다_작으면_재고를_바꿀_수_없다(int quantity) {
        Product product = ProductFixture.product().stock(3).create();

        assertThatThrownBy(() -> product.decreaseStock(quantity))
            .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> product.increaseStock(quantity))
            .isInstanceOf(CustomException.class);

        assertThat(product.getStock()).isEqualTo(3);
    }

    private Product register(String name, String description, int stock) {
        return Product.register(
            name,
            description,
            stock,
            new Price(1000),
            new ShippingPolicy(3000, 1)
        );
    }

}
