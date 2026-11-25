package com.ttadak.order.domain;

import com.ttadak.shared.error.CustomException;
import jakarta.persistence.Embeddable;

@Embeddable
public record Receiver(
    String name,
    String phone,
    String address
) {

    public Receiver {
        if (name == null || name.isBlank()) {
            throw CustomException.validation("receiverName", "받는 사람 이름은 비어 있을 수 없습니다.");
        }
        if (phone == null || phone.isBlank()) {
            throw CustomException.validation("receiverPhone", "받는 사람 연락처는 비어 있을 수 없습니다.");
        }
        if (address == null || address.isBlank()) {
            throw CustomException.validation("receiverAddress", "받는 주소는 비어 있을 수 없습니다.");
        }
    }

}
