package com.ttadak.shared.error;

import java.util.List;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<FieldViolation> errors;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = List.of();
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.errors = List.of();
    }

    private CustomException(ErrorCode errorCode, List<FieldViolation> errors) {
        super(errors.getFirst().reason());
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public static CustomException validation(String field, String reason) {
        return new CustomException(
            ErrorCode.DEFAULT_VALIDATION_FAILED,
            List.of(new FieldViolation(field, reason))
        );
    }

}
