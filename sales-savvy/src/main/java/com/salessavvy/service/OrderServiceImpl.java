
package com.salessavvy.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.AdminOrderDetailsResponse;
import com.salessavvy.dto.AdminOrderItemDetailsResponse;
import com.salessavvy.dto.CouponValidationResponse;
import com.salessavvy.dto.OrderItemResponse;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.PlaceOrderRequest;

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
    private final OrderTrackingService trackingService;

    @Value("${app.delivery.cutoff-hour:14}")
    private int deliveryCutoffHour;

    @Value("${app.delivery.transit-business-days:2}")
    private int transitBusinessDays;

    private static final LocalTime DELIVERY_START_TIME =
            LocalTime.of(10, 0);

    private static final LocalTime DELIVERY_END_TIME =
            LocalTime.of(18, 0);

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PaymentRepository paymentRepository,
            InventoryMovementRepository movementRepository,
            CouponService couponService,
            OrderTrackingService trackingService) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.movementRepository = movementRepository;
        this.couponService = couponService;
        this.trackingService = trackingService;
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));
    }

    // --------------------------------------------------
    // DELIVERY SCHEDULING
    // --------------------------------------------------

    private void calculateDeliverySchedule(Order order) {

        LocalDateTime orderDate = order.getOrderDate() != null
                ? order.getOrderDate()
                : LocalDateTime.now();

        LocalDate deliveryDate = orderDate.toLocalDate();

        if (orderDate.getHour() >= deliveryCutoffHour) {
            deliveryDate = deliveryDate.plusDays(1);
        }

        int addedBusinessDays = 0;

        while (addedBusinessDays < transitBusinessDays) {

            deliveryDate = deliveryDate.plusDays(1);

            if (deliveryDate.getDayOfWeek().getValue() <= 5) {
                addedBusinessDays++;
            }
        }

        order.setEstimatedDeliveryDate(deliveryDate);
        order.setDeliveryStartTime(DELIVERY_START_TIME);
        order.setDeliveryEndTime(DELIVERY_END_TIME);

        order.setDeliveryRemarks(
                "Estimated delivery within the scheduled "
                + "10:00 AM to 6:00 PM delivery window.");
    }

    // --------------------------------------------------
    // RESPONSE MAPPING
    // --------------------------------------------------

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

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getCouponCode(),
                items);

        response.setEstimatedDeliveryDate(
                order.getEstimatedDeliveryDate());

        response.setDeliveryStartTime(
                order.getDeliveryStartTime());

        response.setDeliveryEndTime(
                order.getDeliveryEndTime());

        response.setDeliveredAt(
                order.getDeliveredAt());

        response.setDeliveryRemarks(
                order.getDeliveryRemarks());

        return response;
    }

    // --------------------------------------------------
    // INVENTORY MOVEMENT
    // --------------------------------------------------

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

    // --------------------------------------------------
    // ADMIN ORDER DETAILS
    // --------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailsResponse getAdminOrderDetails(
            Long orderId) {

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
                                item.getSubtotal()))
                        .collect(Collectors.toList());

        List<Payment> payments =
                paymentRepository.findByOrder_IdOrderByCreatedAtDesc(
                        orderId);

        Payment payment = payments.isEmpty()
                ? null
                : payments.get(0);

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
                items);
    }

    // --------------------------------------------------
    // PLACE ORDER
    // --------------------------------------------------

    @Override
    public OrderResponse placeOrder(
            String email,
            PlaceOrderRequest request) {

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

        // Order date is initialized by Order.java @PrePersist.
        // Calculate delivery using the current time before saving.
        calculateDeliverySchedule(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : sortedCartItems) {

            Long productId = cartItem.getProduct().getId();

            Product product = productRepository.findByIdForUpdate(
                    productId)
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
                        "Invalid product quantity: "
                                + product.getName());
            }

            int previousStock = product.getStockQuantity();

            if (quantity > previousStock) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for: "
                                + product.getName());
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

        // Apply coupon if provided.
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

        // Record initial tracking event.
        trackingService.recordStatusChange(
                savedOrder,
                null,
                OrderStatus.PENDING,
                email,
                "Order placed successfully. Delivery estimate calculated.");

        // Clear cart after successful order creation.
        cart.getItems().clear();

        return convertToResponse(savedOrder);
    }

    // --------------------------------------------------
    // CUSTOMER ORDER DETAILS
    // --------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            String email,
            Long orderId) {

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

    // --------------------------------------------------
    // CUSTOMER ORDER HISTORY
    // --------------------------------------------------

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

    // --------------------------------------------------
    // CANCEL ORDER
    // --------------------------------------------------

    @Override
    public OrderResponse cancelOrder(
            String email,
            Long orderId) {

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

        OrderStatus previousStatus = order.getStatus();

        List<Payment> payments =
                paymentRepository.findByOrderIdForUpdate(orderId);

        for (Payment payment : payments) {

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.REFUNDED);

            } else if (payment.getStatus() == PaymentStatus.INITIATED
                    || payment.getStatus() == PaymentStatus.PENDING) {

                payment.setStatus(PaymentStatus.FAILED);
            }
        }

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

        order.setStatus(OrderStatus.CANCELLED);

        order.setDeliveryRemarks(
                "Order cancelled. Reserved stock restored.");

        trackingService.recordStatusChange(
                order,
                previousStatus,
                OrderStatus.CANCELLED,
                email,
                "Order cancelled by customer.");

        Order savedOrder = orderRepository.save(order);

        paymentRepository.saveAll(payments);

        return convertToResponse(savedOrder);
    }

    // --------------------------------------------------
    // ADMIN ORDER STATUS UPDATE
    // --------------------------------------------------

    @Override
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus status) {

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

        OrderStatus previousStatus = order.getStatus();

        boolean validTransition =
                (previousStatus == OrderStatus.CONFIRMED
                        && status == OrderStatus.PROCESSING)
                || (previousStatus == OrderStatus.PROCESSING
                        && status == OrderStatus.SHIPPED)
                || (previousStatus == OrderStatus.SHIPPED
                        && status == OrderStatus.OUT_FOR_DELIVERY)
                || (previousStatus == OrderStatus.OUT_FOR_DELIVERY
                        && status == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order status transition: "
                            + previousStatus + " to " + status);
        }

        order.setStatus(status);

        if (status == OrderStatus.DELIVERED) {

            order.setDeliveredAt(LocalDateTime.now());

            order.setDeliveryRemarks(
                    "Order successfully delivered.");
        }

        trackingService.recordStatusChange(
                order,
                previousStatus,
                status,
                "ADMIN",
                "Order status updated from "
                        + previousStatus + " to " + status);

        return convertToResponse(order);
    }
}
