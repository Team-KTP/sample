package com.ttadak.product.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ttadak.product.application.ProductService;
import com.ttadak.product.application.dto.ModifyProductPrice;
import com.ttadak.product.fixture.ProductFixture;
import com.ttadak.shared.ControllerSliceTest;
import com.ttadak.shared.Covers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ControllerSliceTest(ProductController.class)
class ProductControllerTest {

    private final MockMvcTester mvcTester;

    @MockitoBean
    private ProductService productService;

    ProductControllerTest(MockMvcTester mvcTester) {
        this.mvcTester = mvcTester;
    }

    @Covers("AC-1.1")
    @Test
    void 상품을_등록하면_등록한_상품이_응답된다() {
        given(productService.register(any(), any(), any(), anyInt())).willReturn(
            ProductFixture.product().name("머그컵").stock(0).price(12000).create()
        );

        mvcTester.post()
            .uri("/products")
            .header("memberId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "name": "머그컵",
                  "description": "도자기 머그컵",
                  "stock": 0,
                  "price": 12000,
                  "shippingFee": 3000,
                  "dispatchDays": 2
                }
                """)
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.data.name", v -> v.assertThat().isEqualTo("머그컵"))
            .hasPathSatisfying("$.data.stock", v -> v.assertThat().isEqualTo(0))
            .hasPathSatisfying("$.data.price", v -> v.assertThat().isEqualTo(12000))
            .hasPathSatisfying("$.data.soldOut", v -> v.assertThat().isEqualTo(true));
    }

    @Covers("AC-1.2")
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        name | {"name": "", "description": "설명", "stock": 1, "price": 1000, "shippingFee": 0, "dispatchDays": 1}
        description | {"name": "컵", "description": "", "stock": 1, "price": 1000, "shippingFee": 0, "dispatchDays": 1}
        stock | {"name": "컵", "description": "설명", "stock": -1, "price": 1000, "shippingFee": 0, "dispatchDays": 1}
        price | {"name": "컵", "description": "설명", "stock": 1, "price": -1, "shippingFee": 0, "dispatchDays": 1}
        shippingFee | {"name": "컵", "description": "설명", "stock": 1, "price": 1000, "shippingFee": -1, "dispatchDays": 1}
        dispatchDays | {"name": "컵", "description": "설명", "stock": 1, "price": 1000, "shippingFee": 0, "dispatchDays": 0}
        """)
    void 등록_요청의_값이_규칙에_맞지_않으면_그_필드와_사유로_거절된다(String field, String body) {
        mvcTester.post()
            .uri("/products")
            .header("memberId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying(
                "$.error.code",
                v -> v.assertThat().isEqualTo("DEFAULT_VALIDATION_FAILED")
            )
            .hasPathSatisfying("$.error.data[0].field", v -> v.assertThat().isEqualTo(field));
    }

    @Covers("AC-1.12")
    @Test
    void 가격_수정_요청은_경로의_상품_id와_함께_전달된다() {
        mvcTester.put()
            .uri("/products/1/price")
            .header("ownerId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"price": 1500}
                """)
            .assertThat()
            .hasStatus(HttpStatus.OK);

        then(productService).should().modifyPrice(new ModifyProductPrice(1L, 1500), 1);
    }

    @Covers("AC-1.13")
    @Test
    void 음수_가격으로_수정하는_요청은_가격_필드와_사유로_거절된다() {
        mvcTester.put()
            .uri("/products/1/price")
            .header("ownerId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"price": -1}
                """)
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying("$.error.data[0].field", v -> v.assertThat().isEqualTo("price"));
    }

}
