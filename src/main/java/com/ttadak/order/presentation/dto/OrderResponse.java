package com.ttadak.order.presentation.dto;

import com.ttadak.order.domain.Order;
import com.ttadak.order.domain.OrderState;
import com.ttadak.order.domain.Receiver;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderResponse(
    @Schema(description = "주문 id", example = "1")
    Long id,

    @Schema(description = "주문 상태", example = "ORDERED")
    OrderState state,

    @Schema(description = "상품 id", example = "1")
    Long productId,

    @Schema(description = "주문 수량", example = "2")
    int quantity,

    @Schema(description = "주문 금액", example = "200000")
    int totalPrice,

    @Schema(description = "주문 시각", example = "2026-01-01T12:00:00")
    LocalDateTime orderedAt,

    ReceiverResponse receiver
) {

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .state(order.getState())
            .productId(order.getProductId())
            .quantity(order.getQuantity())
            .totalPrice(order.getTotalPrice())
            .orderedAt(order.getOrderedAt())
            .receiver(ReceiverResponse.from(order.getReceiver()))
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    public record ReceiverResponse(
        @Schema(description = "받는 사람 이름", example = "홍길동")
        String name,

        @Schema(description = "받는 사람 연락처", example = "010-0000-0000")
        String phone,

        @Schema(description = "받는 주소", example = "서울시 어딘가")
        String address
    ) {

        static ReceiverResponse from(Receiver receiver) {
            return ReceiverResponse.builder()
                .name(receiver.name())
                .phone(receiver.phone())
                .address(receiver.address())
                .build();
        }

    }

}
