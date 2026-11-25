package com.ttadak.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ttadak.shared.Covers;
import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PriceTest {

    @Covers("R-1.1")
    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void 가격이_0_이상이면_가격이_만들어진다(int amount) {
        Price price = new Price(amount);

        assertThat(price.amount()).isEqualTo(amount);
    }

    @Covers("R-1.1")
    @Test
    void 가격이_음수이면_거절된다() {
        assertThatThrownBy(() -> new Price(-1))
            .isInstanceOf(CustomException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEFAULT_VALIDATION_FAILED);
    }

}
