package com.ttadak.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.shared.Covers;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ReceiverTest {

    @Covers("R-1.15")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 이름이_비어_있으면_받는_사람이_거절된다(String name) {
        assertRejected(() -> new Receiver(name, "010-0000-0000", "서울시 어딘가"));
    }

    @Covers("R-1.15")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 연락처가_비어_있으면_받는_사람이_거절된다(String phone) {
        assertRejected(() -> new Receiver("홍길동", phone, "서울시 어딘가"));
    }

    @Covers("R-1.15")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 주소가_비어_있으면_받는_사람이_거절된다(String address) {
        assertRejected(() -> new Receiver("홍길동", "010-0000-0000", address));
    }

    private void assertRejected(Runnable creation) {
        assertThatThrownBy(creation::run)
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

}
