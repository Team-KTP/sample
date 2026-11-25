package com.ttadak.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.ttadak.shared.Covers;
import com.ttadak.shared.E2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@E2ETest
class StoreScenarioTest {

    private final RestClient client;

    StoreScenarioTest(@LocalServerPort int port) {
        this.client = RestClient.create("http://localhost:" + port);
    }

    @Covers({"AC-1.1", "AC-1.4", "AC-1.8"})
    @Test
    void 상품을_등록하고_주문한_뒤_취소하면_재고가_처음으로_돌아온다() {
        long productId = post("/products", """
            {
              "name": "머그컵",
              "description": "도자기 머그컵",
              "stock": 5,
              "price": 12000,
              "shippingFee": 3000,
              "dispatchDays": 2
            }
            """).path("data").path("id").asLong();
        long orderId = post("/orders", """
            {
              "productId": %d,
              "quantity": 2,
              "receiverName": "홍길동",
              "receiverPhone": "010-0000-0000",
              "receiverAddress": "서울시 어딘가"
            }
            """.formatted(productId)).path("data").path("id").asLong();
        int stockAfterOrder = stockOf(productId);

        post("/orders/" + orderId + "/cancel", "");

        assertThat(stockAfterOrder).isEqualTo(3);
        assertThat(stockOf(productId)).isEqualTo(5);
    }

    private JsonNode post(String uri, String body) {
        return client.post()
            .uri(uri)
            .header("memberId", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(JsonNode.class);
    }

    private int stockOf(long productId) {
        return client.get()
            .uri("/products/" + productId)
            .retrieve()
            .body(JsonNode.class)
            .path("data")
            .path("stock")
            .asInt();
    }

}
