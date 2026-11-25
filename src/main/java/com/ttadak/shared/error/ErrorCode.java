package com.ttadak.shared.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

/**
 * 서버의 모든 오류 코드. 응답의 {@code code}는 {@link #name()}이다. 기능의 오류 코드는 {@code <기능>_<사유>}로 이름 짓고 기능별로 묶어
 * 아래에 더한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // @formatter:off
    // DEFAULT
    DEFAULT_MALFORMED_REQUEST      (HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", LogLevel.INFO),
    DEFAULT_VALIDATION_FAILED      (HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.", LogLevel.INFO),
    DEFAULT_UNAUTHENTICATED        (HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", LogLevel.WARN),
    DEFAULT_FORBIDDEN              (HttpStatus.FORBIDDEN, "권한이 없습니다.", LogLevel.WARN),
    DEFAULT_NOT_FOUND              (HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", LogLevel.INFO),
    DEFAULT_METHOD_NOT_ALLOWED     (HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다.", LogLevel.INFO),
    DEFAULT_CONFLICT               (HttpStatus.CONFLICT, "요청이 현재 상태와 충돌합니다.", LogLevel.INFO),
    DEFAULT_UNSUPPORTED_MEDIA_TYPE (HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 요청 본문 형식입니다.", LogLevel.INFO),
    DEFAULT_INTERNAL_ERROR         (HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", LogLevel.ERROR),

    // PRODUCT
    PRODUCT_NOT_FOUND              (HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.", LogLevel.INFO),
    PRODUCT_OUT_OF_STOCK           (HttpStatus.CONFLICT, "재고가 부족합니다.", LogLevel.INFO),

    // ORDER
    ORDER_NOT_FOUND                (HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.", LogLevel.INFO),
    ORDER_ALREADY_CANCELED         (HttpStatus.CONFLICT, "이미 취소된 주문입니다.", LogLevel.INFO),

    // @formatter:on
    ;

    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;

}
