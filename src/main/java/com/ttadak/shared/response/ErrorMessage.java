package com.ttadak.shared.response;

import com.ttadak.shared.error.ErrorCode;

/**
 * 오류 응답의 {@code error}. {@code data}에는 입력 검증 실패의 필드별 사유 목록 같은 부가 정보가 담기고, 없으면 null이다.
 */
public record ErrorMessage(
    int status,
    String code,
    String message,
    Object data
) {

    public ErrorMessage(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ErrorMessage(ErrorCode errorCode, Object data) {
        this(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), data);
    }

}
