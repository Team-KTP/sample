package com.ttadak.shared.error;

public record FieldViolation(
    String field,
    String reason
) {

}
