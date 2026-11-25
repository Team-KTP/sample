package com.ttadak.product.domain;

import com.ttadak.shared.error.CustomException;
import jakarta.persistence.Embeddable;

@Embeddable
public record Price(int amount) {

    public Price {
        if (amount < 0) {
            throw CustomException.validation("price", "가격은 0 이상이어야 합니다.");
        }
    }

}
