
package com.salessavvy.service;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.AddressResponse;
import com.salessavvy.dto.CheckoutRequest;
import com.salessavvy.dto.CheckoutResponse;
import com.salessavvy.dto.CouponValidationResponse;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.PaymentRequest;
import com.salessavvy.dto.PaymentResponse;
import com.salessavvy.dto.PlaceOrderRequest;

import com.salessavvy.entity.Order;

import com.salessavvy.repository.OrderRepository;

@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderRepository orderRepository;
    private final AddressService addressService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CheckoutServiceImpl(
            OrderRepository orderRepository,
            AddressService addressService,
            CouponService couponService,
            OrderService orderService,
            PaymentService paymentService) {

        this.orderRepository = orderRepository;
        this.addressService = addressService;
        this.couponService = couponService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @Override
    public CheckoutResponse checkout(
            String email,
            CheckoutRequest request) {

        // 1. Verify saved address belongs to customer
        AddressResponse address = addressService.getAddressById(
                email, request.getAddressId());

        // 2. Build shipping address
        String shippingAddress = buildShippingAddress(address);

        // 3. Create order and reserve stock
        PlaceOrderRequest orderRequest = new PlaceOrderRequest();
        orderRequest.setShippingAddress(shippingAddress);

        OrderResponse createdOrder = orderService.placeOrder(
                email, orderRequest);

        // 4. Retrieve the saved order
        Order order = orderRepository.findById(createdOrder.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Created order not found"));

        // Use the actual order total calculated using locked products
        BigDecimal subtotal = order.getTotalAmount();

        String couponCode = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal;

        // 5. Validate and apply coupon
        if (request.getCouponCode() != null
                && !request.getCouponCode().isBlank()) {

            CouponValidationResponse couponResult =
                    couponService.validateCoupon(
                            request.getCouponCode().trim(),
                            subtotal);

            if (couponResult == null || !couponResult.isValid()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Coupon validation failed");
            }

            couponCode = couponResult.getCouponCode();
            discountAmount = couponResult.getDiscountAmount();
            finalAmount = couponResult.getFinalAmount();

            if (finalAmount == null
                    || discountAmount == null
                    || finalAmount.compareTo(BigDecimal.ZERO) < 0
                    || finalAmount.compareTo(subtotal) > 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid coupon discount calculation");
            }
        }

        // 6. Save discounted amount and coupon code
        order.setCouponCode(couponCode);
        order.setTotalAmount(finalAmount);

        orderRepository.save(order);

        // 7. Get final order response
        OrderResponse finalOrder = orderService.getOrderById(
                email, order.getId());

        // 8. Initiate payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());

        PaymentResponse payment = paymentService.initiatePayment(
                email, paymentRequest);

        // 9. Return checkout response
        return new CheckoutResponse(
                "Checkout completed and payment initiated successfully",
                order.getId(),
                subtotal,
                couponCode,
                discountAmount,
                finalAmount,
                finalOrder,
                payment);
    }

    private String buildShippingAddress(AddressResponse address) {

        StringBuilder result = new StringBuilder();

        append(result, address.getRecipientName());
        append(result, address.getPhoneNumber());
        append(result, address.getAddressLine1());
        append(result, address.getAddressLine2());
        append(result, address.getLandmark());
        append(result, address.getCity());
        append(result, address.getState());
        append(result, address.getPostalCode());
        append(result, address.getCountry());

        return result.toString();
    }

    private void append(StringBuilder result, String value) {

        if (value != null && !value.isBlank()) {

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(value.trim());
        }
    }
}
