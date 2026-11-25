package com.ttadak.order.application.dto;

public record NewOrderItem(
    Long productId,
    int quantity
) {

}
