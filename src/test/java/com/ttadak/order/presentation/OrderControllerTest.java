package com.ttadak.order.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

import com.ttadak.order.application.OrderService;
import com.ttadak.order.fixture.OrderFixture;
import com.ttadak.shared.ControllerSliceTest;
import com.ttadak.shared.Covers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ControllerSliceTest(OrderController.class)
class OrderControllerTest {

    private final MockMvcTester mvcTester;

    @MockitoBean
    private OrderService orderService;

    OrderControllerTest(MockMvcTester mvcTester) {
        this.mvcTester = mvcTester;
    }

    @Covers("AC-1.4")
    @Test
    void 주문하면_주문한_상품의_id와_받는_사람이_함께_응답된다() {
        given(orderService.create(any(), any(), anyInt())).willReturn(
            OrderFixture.order()
                .productId(1L)
                .unitPrice(1000)
                .quantity(2)
                .place()
        );

        mvcTester.post()
            .uri("/orders")
            .header("memberId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "productId": 1,
                  "quantity": 2,
                  "receiverName": "홍길동",
                  "receiverPhone": "010-0000-0000",
                  "receiverAddress": "서울시 어딘가"
                }
                """)
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.data.state", v -> v.assertThat().isEqualTo("ORDERED"))
            .hasPathSatisfying("$.data.totalPrice", v -> v.assertThat().isEqualTo(2000))
            .hasPathSatisfying("$.data.productId", v -> v.assertThat().isEqualTo(1))
            .hasPathSatisfying("$.data.receiver.name", v -> v.assertThat().isEqualTo("홍길동"));
    }

    @Covers("AC-1.6")
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "0 | 홍길동 | quantity",
        "1 | ''  | receiverName"
    })
    void 주문_요청의_값이_규칙에_맞지_않으면_그_필드와_사유로_거절된다(int quantity, String receiverName, String field) {
        mvcTester.post()
            .uri("/orders")
            .header("memberId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "productId": 1,
                  "quantity": %d,
                  "receiverName": "%s",
                  "receiverPhone": "010-0000-0000",
                  "receiverAddress": "서울시 어딘가"
                }
                """.formatted(quantity, receiverName))
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .hasPathSatisfying(
                "$.error.code",
                v -> v.assertThat().isEqualTo("DEFAULT_VALIDATION_FAILED")
            )
            .hasPathSatisfying("$.error.data[0].field", v -> v.assertThat().isEqualTo(field));
    }

}
