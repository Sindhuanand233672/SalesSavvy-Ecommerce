
package com.salessavvy.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.razorpay.RazorpayException;
import com.salessavvy.dto.PaymentRequest;
import com.salessavvy.dto.PaymentResponse;
import com.salessavvy.dto.RazorpayVerificationRequest;
import com.salessavvy.entity.InventoryMovement;
import com.salessavvy.entity.InventoryMovementType;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.Payment;
import com.salessavvy.entity.PaymentMethod;
import com.salessavvy.entity.PaymentStatus;
import com.salessavvy.entity.Product;
import com.salessavvy.entity.User;
import com.salessavvy.entity.UserRole;
import com.salessavvy.repository.InventoryMovementRepository;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.PaymentRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.UserRepository;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;
    private final CouponService couponService;
    private final RazorpayService razorpayService;
    private final OrderTrackingService orderTrackingService;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            InventoryMovementRepository movementRepository,
            CouponService couponService,
            RazorpayService razorpayService,
            OrderTrackingService orderTrackingService) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
        this.couponService = couponService;
        this.razorpayService = razorpayService;
        this.orderTrackingService = orderTrackingService;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private User getAdmin(String email) {
        User user = getUser(email);

        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can perform refunds");
        }

        return user;
    }

    private void verifyOwnership(Order order, String email) {
        User user = getUser(email);

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another user's order");
        }
    }

    /*
     * Confirms an order and records the status transition
     * in the order tracking history.
     */
    private void confirmOrder(Order order, String email) {

        OrderStatus previousStatus = order.getStatus();

        order.setStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);

        orderTrackingService.recordStatusChange(
                order,
                previousStatus,
                OrderStatus.CONFIRMED,
                email,
                "Payment completed successfully. Order confirmed.");
    }

    private PaymentResponse convertToResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getPaymentDate(),
                payment.getCreatedAt());

        response.setRazorpayOrderId(payment.getRazorpayOrderId());
        response.setRazorpayPaymentId(payment.getRazorpayPaymentId());

        if (payment.getPaymentMethod() == PaymentMethod.RAZORPAY) {
            response.setRazorpayKeyId(razorpayService.getKeyId());
        }

        return response;
    }

    private void recordCouponUsage(Order order) {

        if (order.getCouponCode() != null
                && !order.getCouponCode().isBlank()) {

            couponService.recordCouponUsage(order.getCouponCode());
        }
    }

    private void releaseReservedStock(
            Order order, String email, String reason) {

        List<OrderItem> items = order.getOrderItems()
                .stream()
                .sorted(Comparator.comparing(
                        item -> item.getProduct().getId()))
                .collect(Collectors.toList());

        for (OrderItem item : items) {

            Product product = productRepository.findByIdForUpdate(
                    item.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            int previousStock = product.getStockQuantity();
            int newStock;

            try {
                newStock = Math.addExact(
                        previousStock, item.getQuantity());

            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock quantity overflow for: "
                                + product.getName());
            }

            product.setStockQuantity(newStock);

            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(product);
            movement.setMovementType(
                    InventoryMovementType.STOCK_ADDED);
            movement.setQuantity(item.getQuantity());
            movement.setPreviousStock(previousStock);
            movement.setNewStock(newStock);
            movement.setReason(reason + order.getId());
            movement.setPerformedBy(email);

            movementRepository.save(movement);
        }
    }

@Override
public PaymentResponse collectCodPayment(
        String email, Long orderId) {

    // Verify that the requester is an administrator
    getAdmin(email);

    // Lock the order before updating its payment
    Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Order not found"));

    // COD collection is allowed only after delivery
    if (order.getStatus() != OrderStatus.DELIVERED) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "COD payment can only be collected for delivered orders");
    }

    // Find the latest payment for this order
    List<Payment> payments =
            paymentRepository.findByOrder_IdOrderByCreatedAtDesc(orderId);

    if (payments.isEmpty()) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Payment not found for this order");
    }

    Payment latestPayment = payments.get(0);

    // Lock the payment before updating it
    Payment payment = paymentRepository.findByIdForUpdate(
            latestPayment.getId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Payment not found"));

    // Only COD payments can be collected through this operation
    if (payment.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This order does not have a Cash on Delivery payment");
    }

    // Prevent duplicate collection
    if (payment.getStatus() != PaymentStatus.PENDING) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "COD payment is not pending or has already been collected");
    }

    // Record successful COD collection
    payment.setStatus(PaymentStatus.SUCCESS);
    payment.setPaymentDate(LocalDateTime.now());

    payment = paymentRepository.save(payment);

    return convertToResponse(payment);
}


    @Override
    public PaymentResponse initiatePayment(
            String email, PaymentRequest request) {

        Order order = orderRepository.findByIdForUpdate(
                request.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        verifyOwnership(order, email);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment can only be initiated for pending orders");
        }

        List<Payment> existingPayments =
                paymentRepository.findByOrder_Id(order.getId());

        boolean paymentAlreadyCompleted = existingPayments.stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS
                        || p.getStatus() == PaymentStatus.REFUND_PENDING
                        || p.getStatus() == PaymentStatus.REFUNDED);

        if (paymentAlreadyCompleted) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This order already has a completed payment");
        }

        boolean paymentInProgress = existingPayments.stream()
                .anyMatch(p ->
                        p.getStatus() == PaymentStatus.INITIATED
                        || p.getStatus() == PaymentStatus.PENDING);

        if (paymentInProgress) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A payment is already in progress for this order");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setTransactionId(
                "TXN-" + UUID.randomUUID().toString().toUpperCase());

        payment = paymentRepository.save(payment);

        if (request.getPaymentMethod() == PaymentMethod.RAZORPAY) {

            try {
                com.razorpay.Order razorpayOrder =
                        razorpayService.createOrder(
                                payment.getAmount(),
                                "payment_" + payment.getId());

                payment.setRazorpayOrderId(
                        razorpayOrder.get("id").toString());

                payment = paymentRepository.save(payment);

            } catch (RazorpayException | RuntimeException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Unable to create Razorpay order. Please try again.");
            }

        } else if (request.getPaymentMethod()
                == PaymentMethod.CASH_ON_DELIVERY) {

            payment.setStatus(PaymentStatus.PENDING);

            confirmOrder(order, email);

            recordCouponUsage(order);

            payment = paymentRepository.save(payment);

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported payment method");
        }

        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(
            String email, Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        verifyOwnership(payment.getOrder(), email);

        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrder(
            String email, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        verifyOwnership(order, email);

        return paymentRepository
                .findByOrder_IdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse verifyRazorpayPayment(
            String email, RazorpayVerificationRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Razorpay order not found"));

        Long orderId = payment.getOrder().getId();

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        verifyOwnership(order, email);

        payment = paymentRepository.findByIdForUpdate(payment.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getPaymentMethod() != PaymentMethod.RAZORPAY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This is not a Razorpay payment");
        }

        if (!request.getRazorpayOrderId()
                .equals(payment.getRazorpayOrderId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Razorpay order ID does not match");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            if (request.getRazorpayPaymentId()
                    .equals(payment.getRazorpayPaymentId())) {
                return convertToResponse(payment);
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This payment has already been completed");
        }

        if (payment.getStatus() != PaymentStatus.INITIATED
                || order.getStatus() != OrderStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment is not in a verifiable state");
        }

        boolean signatureValid =
                razorpayService.verifyPaymentSignature(
                        request.getRazorpayOrderId(),
                        request.getRazorpayPaymentId(),
                        request.getRazorpaySignature());

        if (!signatureValid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Razorpay payment signature");
        }

        boolean paymentCaptured =
                razorpayService.verifyCapturedPayment(
                        request.getRazorpayPaymentId(),
                        payment.getRazorpayOrderId(),
                        payment.getAmount());

        if (!paymentCaptured) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Razorpay payment is not captured or payment details do not match");
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setTransactionId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        confirmOrder(order, email);

        recordCouponUsage(order);

        payment = paymentRepository.save(payment);

        return convertToResponse(payment);
    }

    @Override
    public PaymentResponse failPayment(
            String email, Long paymentId) {

        Payment initialPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        Long orderId = initialPayment.getOrder().getId();

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        verifyOwnership(order, email);

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getPaymentMethod() != PaymentMethod.RAZORPAY) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This operation is only for Razorpay payments");
        }

        if (payment.getStatus() != PaymentStatus.INITIATED
                || order.getStatus() != OrderStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment is not in a pending verification state");
        }

        if (payment.getRazorpayPaymentId() == null
                || payment.getRazorpayOrderId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment status cannot be verified yet. "
                            + "The order remains pending.");
        }

        String razorpayStatus;

        try {
            razorpayStatus = razorpayService.getVerifiedPaymentStatus(
                    payment.getRazorpayPaymentId(),
                    payment.getRazorpayOrderId(),
                    payment.getAmount());

        } catch (RazorpayException | RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify payment status with Razorpay. "
                            + "The order remains pending.");
        }

        if ("captured".equalsIgnoreCase(razorpayStatus)) {

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());

            confirmOrder(order, email);

            recordCouponUsage(order);

        } else if ("failed".equalsIgnoreCase(razorpayStatus)) {

            releaseReservedStock(
                    order,
                    email,
                    "Reserved stock released after confirmed failed payment for order #");

            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);

        } else {
            return convertToResponse(payment);
        }

        orderRepository.save(order);

        return convertToResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse refundPayment(
            String email, Long paymentId) {

        getAdmin(email);

        Payment initialPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        Long orderId = initialPayment.getOrder().getId();

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only successful payments can be refunded");
        }

        if (payment.getPaymentMethod() != PaymentMethod.RAZORPAY
                || payment.getRazorpayPaymentId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid Razorpay payment is required for this refund");
        }

        if (payment.getRazorpayRefundId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A refund has already been initiated");
        }

        if (order.getStatus() == OrderStatus.REFUNDED
                || order.getStatus() == OrderStatus.CANCELLED) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This order cannot be refunded");
        }

        try {
            com.razorpay.Refund refund =
                    razorpayService.createFullRefund(
                            payment.getRazorpayPaymentId());

            String refundId = refund.get("id").toString();
            String refundStatus = refund.get("status").toString();

            payment.setRazorpayRefundId(refundId);

            if ("processed".equalsIgnoreCase(refundStatus)) {

                payment.setStatus(PaymentStatus.REFUNDED);
                order.setStatus(OrderStatus.REFUNDED);

            } else {
                payment.setStatus(PaymentStatus.REFUND_PENDING);
            }

            orderRepository.save(order);
            payment = paymentRepository.save(payment);

            return convertToResponse(payment);

        } catch (RazorpayException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Razorpay refund request failed. Check the Razorpay "
                            + "Dashboard before attempting another refund.");
        }
    }
}
