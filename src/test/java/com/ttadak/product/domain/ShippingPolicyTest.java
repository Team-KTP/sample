package com.ttadak.product.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.shared.Covers;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShippingPolicyTest {

    @Covers({"R-1.13", "R-1.14"})
    @ParameterizedTest
    @CsvSource({"-1, 1", "0, 0"})
    void 배송비가_음수이거나_출고_소요일이_1보다_작으면_거절된다(int shippingFee, int dispatchDays) {
        assertThatThrownBy(() -> new ShippingPolicy(shippingFee, dispatchDays))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

}
