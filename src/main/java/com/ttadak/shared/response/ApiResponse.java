package com.ttadak.shared.response;

import com.ttadak.shared.error.ErrorCode;

/**
 * 모든 API의 공통 응답. 성공이면 {@code data}에 값이 담기고 {@code error}는 null이다. 실패면 반대다. 실패 응답은 HTTP 상태도 오류 코드의
 * 상태로 내려간다.
 */
public record ApiResponse<T>(
    ResultType result,
    T data,
    ErrorMessage error
) {

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorCode));
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, Object data) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorCode, data));
    }

}
