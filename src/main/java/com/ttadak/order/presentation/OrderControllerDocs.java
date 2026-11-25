package com.ttadak.order.presentation;

import com.ttadak.order.presentation.dto.OrderCreateRequest;
import com.ttadak.order.presentation.dto.OrderResponse;
import com.ttadak.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order", description = "주문 생성, 조회, 취소")
public abstract class OrderControllerDocs {

    @Operation(summary = "주문", description = """
        상품을 주문하고 만든 주문을 돌려준다.

        - 주문 수량은 1 이상이다.
        - 상품이 없으면 PRODUCT_NOT_FOUND(404)다.
        - 재고가 모자라면 PRODUCT_OUT_OF_STOCK(409)다.
        """)
    public abstract ApiResponse<OrderResponse> create(
        @Parameter(in = ParameterIn.HEADER, description = "요청한 회원의 id", example = "1") int memberId,
        OrderCreateRequest request
    );

    @Operation(summary = "주문 조회", description = "id로 주문 하나를 조회한다. 없으면 ORDER_NOT_FOUND(404)다.")
    public abstract ApiResponse<OrderResponse> findOrder(
        @Parameter(description = "주문 id", example = "1") Long orderId
    );

    @Operation(summary = "주문 취소", description = """
        주문을 취소하고 재고를 되돌린다.

        - 이미 취소한 주문이면 ORDER_ALREADY_CANCELED(409)다.
        """)
    public abstract ApiResponse<Void> cancel(
        @Parameter(description = "주문 id", example = "1") Long orderId
    );

}
