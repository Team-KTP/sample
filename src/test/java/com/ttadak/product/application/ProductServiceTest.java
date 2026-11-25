package com.ttadak.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.product.application.dto.ModifyProductPrice;
import com.ttadak.product.application.dto.NewProductContent;
import com.ttadak.product.application.dto.NewProductSales;
import com.ttadak.product.application.dto.NewProductShipping;
import com.ttadak.product.domain.Price;
import com.ttadak.product.domain.Product;
import com.ttadak.product.domain.ShippingPolicy;
import com.ttadak.product.fixture.ProductFixture;
import com.ttadak.product.infrastructure.ProductRepository;
import com.ttadak.shared.Covers;
import com.ttadak.shared.IntegrationTest;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import org.junit.jupiter.api.Test;

@IntegrationTest
class ProductServiceTest {

    private final ProductService productService;
    private final ProductRepository productRepository;

    ProductServiceTest(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Covers("AC-1.1")
    @Test
    void 상품을_등록하면_상품이_저장된다() {
        Product registered = productService.register(
            new NewProductContent("머그컵", "도자기 머그컵"),
            new NewProductSales(5, 12000),
            new NewProductShipping(3000, 2),
            1
        );

        Product saved = productRepository.findById(registered.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("머그컵");
        assertThat(saved.getStock()).isEqualTo(5);
        assertThat(saved.getPrice()).isEqualTo(new Price(12000));
        assertThat(saved.getDescription()).isEqualTo("도자기 머그컵");
        assertThat(saved.getShippingPolicy()).isEqualTo(new ShippingPolicy(3000, 2));
    }

    @Covers("AC-1.12")
    @Test
    void 가격을_수정하면_바뀐_가격이_저장된다() {
        Product product = productRepository.save(ProductFixture.product().price(1000).create());

        productService.modifyPrice(new ModifyProductPrice(product.getId(), 1500), 1);

        Product saved = productRepository.findById(product.getId()).orElseThrow();
        assertThat(saved.getPrice()).isEqualTo(new Price(1500));
    }

    @Covers("AC-1.12")
    @Test
    void 없는_상품의_가격을_수정하면_상품_없음으로_거절된다() {
        var modifyProductPrice = new ModifyProductPrice(999L, 1500);

        assertThatThrownBy(() -> productService.modifyPrice(modifyProductPrice, 1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Covers("AC-1.3")
    @Test
    void 없는_상품을_조회하면_상품_없음으로_거절된다() {
        assertThatThrownBy(() -> productService.findProduct(999L))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

}
