package com.ttadak.product.presentation.dto;

import com.ttadak.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "상품 이름", example = "노트북")
    String name,

    @Schema(description = "상품 설명", example = "노트북입니다.")
    String description,

    @Schema(description = "재고", example = "10")
    int stock,

    @Schema(description = "가격", example = "100000")
    int price,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "배송비", example = "2500")
    int shippingFee,

    @Schema(description = "출고 소요일", example = "3")
    int dispatchDays
) {

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .stock(product.getStock())
            .price(product.getPrice().amount())
            .soldOut(product.isSoldOut())
            .shippingFee(product.getShippingPolicy().shippingFee())
            .dispatchDays(product.getShippingPolicy().dispatchDays())
            .build();
    }

}
