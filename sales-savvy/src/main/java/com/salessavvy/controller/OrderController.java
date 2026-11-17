
package com.salessavvy.controller;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.PlaceOrderRequest;
import com.salessavvy.service.OrderService;
import com.salessavvy.entity.OrderStatus;
@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Place a new order
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request) {

        OrderResponse response = orderService.placeOrder(
                authentication.getName(), request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get all orders of logged-in user
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrders(authentication.getName()));
    }

    // Get a particular order
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            Authentication authentication,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrderById(
                        authentication.getName(), orderId));
    }

    // Cancel an order
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.cancelOrder(
                        authentication.getName(), orderId));
    }
}
