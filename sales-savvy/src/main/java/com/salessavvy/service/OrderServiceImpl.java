
package com.salessavvy.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.OrderItemResponse;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.PlaceOrderRequest;
import com.salessavvy.dto.CouponValidationResponse;

import com.salessavvy.entity.Cart;
import com.salessavvy.entity.CartItem;
import com.salessavvy.entity.InventoryMovement;
import com.salessavvy.entity.InventoryMovementType;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.Payment;
import com.salessavvy.entity.PaymentStatus;
import com.salessavvy.entity.Product;
import com.salessavvy.entity.User;

import com.salessavvy.repository.CartRepository;
import com.salessavvy.repository.InventoryMovementRepository;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.PaymentRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.UserRepository;
import com.salessavvy.dto.AdminOrderDetailsResponse;
import com.salessavvy.dto.AdminOrderItemDetailsResponse;
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryMovementRepository movementRepository;
    private final CouponService couponService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            InventoryMovementRepository movementRepository,
            CouponService couponService) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.movementRepository = movementRepository;
        this.couponService = couponService;
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));
    }

    private OrderResponse convertToResponse(Order order) {

        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getCouponCode(),
                items);
    }

    private void recordMovement(
            Product product,
            InventoryMovementType type,
            int quantity,
            int previousStock,
            int newStock,
            String reason,
            String email) {

        InventoryMovement movement = new InventoryMovement();

        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(reason);
        movement.setPerformedBy(email);

        movementRepository.save(movement);
    }
    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailsResponse getAdminOrderDetails(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"));

        List<AdminOrderItemDetailsResponse> items =
                order.getOrderItems().stream()
                        .map(item -> new AdminOrderItemDetailsResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getSku(),
                                item.getProduct().getImageUrl(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .collect(Collectors.toList());

        List<Payment> payments =
                paymentRepository.findByOrder_IdOrderByCreatedAtDesc(orderId);

        Payment payment = payments.isEmpty() ? null : payments.get(0);

        return new AdminOrderDetailsResponse(
                order.getId(),
                order.getUser().getFullName(),
                order.getUser().getEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                order.getShippingAddress(),
                order.getCouponCode(),
                payment != null ? payment.getPaymentMethod() : null,
                payment != null ? payment.getStatus() : null,
                payment != null ? payment.getTransactionId() : null,
                payment != null ? payment.getPaymentDate() : null,
                items
        );
    }

    @Override
    public OrderResponse placeOrder(
            String email, PlaceOrderRequest request) {

        User user = getUser(email);

        Cart cart = cartRepository.findByUser_IdForUpdate(user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot place an order with an empty cart");
        }

        List<CartItem> sortedCartItems = cart.getItems()
                .stream()
                .sorted((a, b) -> Long.compare(
                        a.getProduct().getId(),
                        b.getProduct().getId()))
                .collect(Collectors.toList());

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : sortedCartItems) {

            Long productId = cartItem.getProduct().getId();

            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Product not found"));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product is inactive: " + product.getName());
            }

            if (product.getCategory() == null
                    || !Boolean.TRUE.equals(
                            product.getCategory().getActive())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product category is inactive: "
                                + product.getName());
            }

            int quantity = cartItem.getQuantity();

            if (quantity <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid product quantity: " + product.getName());
            }

            int previousStock = product.getStockQuantity();

            if (quantity > previousStock) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for: " + product.getName());
            }

            BigDecimal purchasePrice = product.getPrice();

            BigDecimal subtotal = purchasePrice.multiply(
                    BigDecimal.valueOf(quantity));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(purchasePrice);

            order.getOrderItems().add(orderItem);

            int newStock = previousStock - quantity;
            product.setStockQuantity(newStock);

            recordMovement(
                    product,
                    InventoryMovementType.STOCK_REMOVED,
                    quantity,
                    previousStock,
                    newStock,
                    "Stock reserved for order checkout",
                    email);

            totalAmount = totalAmount.add(subtotal);
        }

        // Apply coupon if provided
        String couponCode = request.getCouponCode();

        if (couponCode != null && !couponCode.isBlank()) {

            CouponValidationResponse coupon =
                    couponService.validateCoupon(
                            couponCode,
                            totalAmount);

            if (!coupon.isValid()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        coupon.getMessage());
            }

            order.setCouponCode(coupon.getCouponCode());
            order.setTotalAmount(coupon.getFinalAmount());

        } else {
            order.setTotalAmount(totalAmount);
        }

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();

        return convertToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            String email, Long orderId) {

        User user = getUser(email);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another user's order");
        }

        return convertToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {

        User user = getUser(email);

        return orderRepository
                .findByUser_IdOrderByOrderDateDesc(user.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse cancelOrder(String email, Long orderId) {

        User user = getUser(email);

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot cancel another user's order");
        }

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only pending or confirmed orders can be cancelled");
        }

        // Lock all payments associated with this order.
        List<Payment> payments =
                paymentRepository.findByOrderIdForUpdate(orderId);

        // Update payment status.
        for (Payment payment : payments) {

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                // Simulated refund for successful payments.
                payment.setStatus(PaymentStatus.REFUNDED);

            } else if (payment.getStatus() == PaymentStatus.INITIATED
                    || payment.getStatus() == PaymentStatus.PENDING) {

                payment.setStatus(PaymentStatus.FAILED);
            }
        }

        // Restore reserved stock exactly once.
        List<OrderItem> sortedItems = order.getOrderItems()
                .stream()
                .sorted((a, b) -> Long.compare(
                        a.getProduct().getId(),
                        b.getProduct().getId()))
                .collect(Collectors.toList());

        for (OrderItem item : sortedItems) {

            Product product = productRepository.findByIdForUpdate(
                    item.getProduct().getId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Product not found"));

            int previousStock = product.getStockQuantity();
            int newStock;

            try {
                newStock = Math.addExact(
                        previousStock,
                        item.getQuantity());

            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock quantity overflow for: "
                                + product.getName());
            }

            product.setStockQuantity(newStock);

            recordMovement(
                    product,
                    InventoryMovementType.STOCK_ADDED,
                    item.getQuantity(),
                    previousStock,
                    newStock,
                    "Stock restored after cancellation of order #"
                            + orderId,
                    email);
        }

        // Mark the order as cancelled.
        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        paymentRepository.saveAll(payments);

        return convertToResponse(savedOrder);
    }
    
    @Override
    public OrderResponse updateOrderStatus(
            Long orderId, OrderStatus status) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order status is required");
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Order not found"));

        OrderStatus currentStatus = order.getStatus();

        boolean validTransition =
                (currentStatus == OrderStatus.CONFIRMED
                        && status == OrderStatus.PROCESSING)
                || (currentStatus == OrderStatus.PROCESSING
                        && status == OrderStatus.SHIPPED)
                || (currentStatus == OrderStatus.SHIPPED
                        && status == OrderStatus.OUT_FOR_DELIVERY)
                || (currentStatus == OrderStatus.OUT_FOR_DELIVERY
                        && status == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order status transition: "
                            + currentStatus + " to " + status);
        }

        order.setStatus(status);

        return convertToResponse(order);
    }
}
