package com.ttadak.product.presentation.dto;

import com.ttadak.product.application.dto.NewProductContent;
import com.ttadak.product.application.dto.NewProductSales;
import com.ttadak.product.application.dto.NewProductShipping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRegisterRequest(
    @NotBlank(message = "상품 이름은 필수입니다.")
    @Schema(description = "상품 이름", example = "노트북")
    String name,

    @NotBlank(message = "상품 설명은 필수입니다.")
    @Schema(description = "상품 설명", example = "노트북입니다.")
    String description,

    @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
    @Schema(description = "재고", example = "10")
    int stock,

    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    @Schema(description = "가격", example = "100000")
    int price,

    @PositiveOrZero(message = "배송비는 0 이상이어야 합니다.")
    @Schema(description = "배송비", example = "2500")
    int shippingFee,

    @Positive(message = "출고 소요일은 1 이상이어야 합니다.")
    @Schema(description = "출고 소요일", example = "3")
    int dispatchDays
) {

    public NewProductContent toNewProductContent() {
        return new NewProductContent(name, description);
    }

    public NewProductSales toNewProductSales() {
        return new NewProductSales(stock, price);
    }

    public NewProductShipping toNewProductShipping() {
        return new NewProductShipping(shippingFee, dispatchDays);
    }

}
