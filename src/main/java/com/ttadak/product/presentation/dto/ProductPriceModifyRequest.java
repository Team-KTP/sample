package com.ttadak.product.presentation.dto;

import com.ttadak.product.application.dto.ModifyProductPrice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductPriceModifyRequest(
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    @Schema(description = "가격", example = "100000")
    int price
) {

    public ModifyProductPrice toModifyProductPrice(Long productId) {
        return new ModifyProductPrice(productId, price);
    }

}
