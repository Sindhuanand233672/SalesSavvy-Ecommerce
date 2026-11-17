
package com.salessavvy.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.AdminCustomerOrderItemResponse;
import com.salessavvy.dto.AdminCustomerOrderResponse;
import com.salessavvy.dto.AdminCustomerDetailsResponse;
import com.salessavvy.dto.AdminCustomerResponse;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.User;
import com.salessavvy.entity.UserRole;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.UserRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/admin/customers")
@SecurityRequirement(name = "bearerAuth")
public class AdminCustomerController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AdminCustomerController(
            UserRepository userRepository,
            OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdminCustomerResponse>> getAllCustomers() {

        List<AdminCustomerResponse> customers =
                userRepository
                        .findAllByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(customers);
    }

    // ACTIVATE OR DEACTIVATE CUSTOMER
    @PutMapping("/{customerId}/status")
    public ResponseEntity<AdminCustomerResponse> updateCustomerStatus(
            @PathVariable Long customerId,
            @RequestParam Boolean active) {

        User customer = userRepository.findById(customerId)
                .filter(user -> user.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));

        if (active == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Active status is required"
            );
        }

        customer.setActive(active);

        User updatedCustomer = userRepository.save(customer);

        return ResponseEntity.ok(mapToResponse(updatedCustomer));
    }

    @GetMapping("/{customerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<AdminCustomerDetailsResponse> getCustomerDetails(
            @PathVariable Long customerId) {

        User customer = userRepository.findById(customerId)
                .filter(user -> user.getRole() == UserRole.CUSTOMER)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));

        List<Order> orders =
                orderRepository.findByUser_IdOrderByOrderDateDesc(customerId);

        List<AdminCustomerOrderResponse> orderResponses =
                orders.stream()
                        .map(this::mapOrderToResponse)
                        .toList();

        BigDecimal totalSpent = orders.stream()
                .filter(order ->
                        order.getStatus() != OrderStatus.CANCELLED
                        && order.getStatus() != OrderStatus.REFUNDED)
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AdminCustomerDetailsResponse response =
                new AdminCustomerDetailsResponse(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getEmail(),
                        customer.getPhoneNumber(),
                        customer.getCreatedAt(),
                        customer.getActive(),
                        orders.size(),
                        totalSpent,
                        orderResponses
                );

        return ResponseEntity.ok(response);
    }

    private AdminCustomerResponse mapToResponse(User user) {

        return new AdminCustomerResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getActive()
        );
    }

    private AdminCustomerOrderResponse mapOrderToResponse(Order order) {

        List<AdminCustomerOrderItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(this::mapOrderItemToResponse)
                        .toList();

        return new AdminCustomerOrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getCouponCode(),
                items
        );
    }

    private AdminCustomerOrderItemResponse mapOrderItemToResponse(
            OrderItem item) {

        return new AdminCustomerOrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getProduct().getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
