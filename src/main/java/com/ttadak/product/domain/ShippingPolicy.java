package com.ttadak.product.domain;

import com.ttadak.shared.error.CustomException;
import jakarta.persistence.Embeddable;

@Embeddable
public record ShippingPolicy(
    int shippingFee,
    int dispatchDays
) {

    public ShippingPolicy {
        if (shippingFee < 0) {
            throw CustomException.validation("shippingFee", "배송비는 0 이상이어야 합니다.");
        }
        if (dispatchDays < 1) {
            throw CustomException.validation("dispatchDays", "출고 소요일은 1 이상이어야 합니다.");
        }
    }

}
