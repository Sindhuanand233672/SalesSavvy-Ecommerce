
package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.AdminOrderDetailsResponse;
import com.salessavvy.dto.AdminOrderResponse;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.Payment;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.PaymentRepository;
import com.salessavvy.service.OrderService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/admin/orders")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public AdminOrderController(
            OrderService orderService,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {

        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    // ADMIN AUTHORIZATION
    private void checkAdmin(Authentication authentication) {

        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can access this endpoint");
        }
    }

    // GET ALL ORDERS
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders(
            Authentication authentication) {

        checkAdmin(authentication);

        List<AdminOrderResponse> orders =
                orderRepository.findAll().stream()
                        .sorted((a, b) ->
                                b.getOrderDate().compareTo(a.getOrderDate()))
                        .map(order -> {

                            List<Payment> payments =
                                    paymentRepository
                                            .findByOrder_IdOrderByCreatedAtDesc(
                                                    order.getId());

                            Payment payment = payments.isEmpty()
                                    ? null
                                    : payments.get(0);

                            return new AdminOrderResponse(
                                    order.getId(),
                                    order.getUser().getFullName(),
                                    order.getUser().getEmail(),
                                    order.getTotalAmount(),
                                    order.getStatus(),
                                    payment != null
                                            ? payment.getPaymentMethod() : null,
                                    payment != null
                                            ? payment.getStatus() : null,
                                    order.getOrderDate()
                            );
                        })
                        .toList();

        return ResponseEntity.ok(orders);
    }

    // UPDATE ORDER STATUS
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        checkAdmin(authentication);

        OrderResponse response =
                orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(response);
    }

    // GET ADMIN ORDER DETAILS
    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDetailsResponse> getOrderDetails(
            Authentication authentication,
            @PathVariable Long orderId) {

        checkAdmin(authentication);

        return ResponseEntity.ok(
                orderService.getAdminOrderDetails(orderId));
    }
}
