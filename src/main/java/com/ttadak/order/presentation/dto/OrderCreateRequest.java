package com.ttadak.order.presentation.dto;

import com.ttadak.order.application.dto.NewOrderItem;
import com.ttadak.order.application.dto.NewOrderReceiver;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderCreateRequest(
    @NotNull(message = "상품 id는 필수입니다.")
    @Schema(description = "상품 id", example = "1")
    Long productId,

    @Positive(message = "주문 수량은 1 이상이어야 합니다.")
    @Schema(description = "주문 수량", example = "2")
    int quantity,

    @NotBlank(message = "받는 사람 이름은 필수입니다.")
    @Schema(description = "받는 사람 이름", example = "홍길동")
    String receiverName,

    @NotBlank(message = "받는 사람 연락처는 필수입니다.")
    @Schema(description = "받는 사람 연락처", example = "010-0000-0000")
    String receiverPhone,

    @NotBlank(message = "받는 주소는 필수입니다.")
    @Schema(description = "받는 주소", example = "서울시 어딘가")
    String receiverAddress
) {

    public NewOrderItem toNewOrderItem() {
        return new NewOrderItem(productId, quantity);
    }

    public NewOrderReceiver toNewOrderReceiver() {
        return new NewOrderReceiver(receiverName, receiverPhone, receiverAddress);
    }

}
