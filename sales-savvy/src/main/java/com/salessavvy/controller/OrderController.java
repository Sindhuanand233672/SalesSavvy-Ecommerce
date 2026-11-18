
package com.salessavvy.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.OrderTrackingResponse;
import com.salessavvy.dto.PlaceOrderRequest;
import com.salessavvy.service.OrderService;
import com.salessavvy.service.OrderTrackingService;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final OrderTrackingService trackingService;

    public OrderController(
            OrderService orderService,
            OrderTrackingService trackingService) {

        this.orderService = orderService;
        this.trackingService = trackingService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request) {

        OrderResponse response = orderService.placeOrder(
                authentication.getName(), request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrders(authentication.getName()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            Authentication authentication,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrderById(
                        authentication.getName(), orderId));
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<List<OrderTrackingResponse>> getOrderTracking(
            Authentication authentication,
            @PathVariable Long orderId) {

        orderService.getOrderById(authentication.getName(), orderId);

        return ResponseEntity.ok(
                trackingService.getTrackingHistory(orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.cancelOrder(
                        authentication.getName(), orderId));
    }
}
