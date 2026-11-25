package com.ttadak.shared.web;

import com.ttadak.shared.error.CustomException;
import com.ttadak.shared.error.ErrorCode;
import com.ttadak.shared.error.FieldViolation;
import com.ttadak.shared.response.ApiResponse;
import com.ttadak.shared.response.ErrorMessage;
import com.ttadak.shared.response.ResultType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        logByLevel(
            errorCode.getLogLevel(),
            "[GlobalExceptionHandler] 예외 응답 - code=%s, message=%s"
                .formatted(errorCode.name(), ex.getMessage()),
            ex
        );

        return ResponseEntity.status(errorCode.getStatus())
            .body(
                ex.getErrors().isEmpty()
                    ? ApiResponse.error(errorCode)
                    : ApiResponse.error(errorCode, ex.getErrors())
            );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        logByLevel(LogLevel.ERROR, "[GlobalExceptionHandler] 처리되지 않은 예외", ex);

        ErrorCode errorCode = ErrorCode.DEFAULT_INTERNAL_ERROR;

        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorCode));
    }

    /**
     * 입력 검증 실패(VALIDATION_FAILED)는 {@link MethodArgumentNotValidException}으로 오고, 부모가 400
     * BAD_REQUEST로 응답을 만든다. 본문만
     * {@link ApiResponse}로 바꾼다.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        List<FieldViolation> violations = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
            .toList();
        ErrorCode errorCode = ErrorCode.DEFAULT_VALIDATION_FAILED;
        logByLevel(
            errorCode.getLogLevel(),
            "[GlobalExceptionHandler] 입력 검증 실패 - code=%s, errors=%s"
                .formatted(errorCode.name(), violations),
            ex
        );

        return super.handleExceptionInternal(
            ex,
            ApiResponse.error(errorCode, violations),
            headers,
            errorCode.getStatus(),
            request
        );
    }

    /**
     * 부모가 만든 표준 예외의 응답(깨진 본문, 타입 불일치, 없는 경로, 지원하지 않는 메서드 등)의 본문을 {@link ApiResponse}로 바꾼다. 상태에 맞는
     * 공통 오류 코드가 없으면 400대는
     * {@code DEFAULT_MALFORMED_REQUEST}, 그 밖은 {@code DEFAULT_INTERNAL_ERROR}의 코드를 붙이고 상태는 그대로 둔다.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
        @NonNull Exception ex,
        @Nullable Object body,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode statusCode,
        @NonNull WebRequest request
    ) {
        ErrorCode errorCode = codeOf(statusCode);
        logByLevel(
            errorCode.getLogLevel(),
            "[GlobalExceptionHandler] 예외 응답 - type=%s, message=%s"
                .formatted(ex.getClass().getSimpleName(), ex.getMessage()),
            ex
        );

        ApiResponse<Void> response = new ApiResponse<>(
            ResultType.ERROR,
            null,
            new ErrorMessage(statusCode.value(), errorCode.name(), errorCode.getMessage(), null)
        );

        return super.handleExceptionInternal(ex, response, headers, statusCode, request);
    }

    private ErrorCode codeOf(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> ErrorCode.DEFAULT_NOT_FOUND;
            case 405 -> ErrorCode.DEFAULT_METHOD_NOT_ALLOWED;
            case 415 -> ErrorCode.DEFAULT_UNSUPPORTED_MEDIA_TYPE;
            default -> status.is5xxServerError()
                ? ErrorCode.DEFAULT_INTERNAL_ERROR
                : ErrorCode.DEFAULT_MALFORMED_REQUEST;
        };
    }

    private void logByLevel(LogLevel level, String message, Exception ex) {
        switch (level) {
            case ERROR -> log.error(message, ex);
            case WARN -> log.warn(message, ex);
            default -> log.info(message, ex);
        }
    }

}
