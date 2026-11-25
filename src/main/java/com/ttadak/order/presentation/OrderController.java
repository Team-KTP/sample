package com.ttadak.order.presentation;

import com.ttadak.order.application.OrderService;
import com.ttadak.order.domain.Order;
import com.ttadak.order.presentation.dto.OrderCreateRequest;
import com.ttadak.order.presentation.dto.OrderResponse;
import com.ttadak.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RequiredArgsConstructor
@RestController
class OrderController extends OrderControllerDocs {

    private final OrderService orderService;

    @Override
    @PostMapping
    public ApiResponse<OrderResponse> create(
        @RequestHeader int memberId,
        @Valid @RequestBody OrderCreateRequest request
    ) {
        Order order = orderService.create(
            request.toNewOrderItem(),
            request.toNewOrderReceiver(),
            memberId
        );

        return ApiResponse.success(OrderResponse.from(order));
    }

    @Override
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> findOrder(@PathVariable Long orderId) {
        Order order = orderService.findOrder(orderId);

        return ApiResponse.success(OrderResponse.from(order));
    }

    @Override
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long orderId) {
        orderService.cancel(orderId);

        return ApiResponse.success();
    }

}
