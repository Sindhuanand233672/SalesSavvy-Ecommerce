package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.PlaceOrderRequest;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.dto.AdminOrderDetailsResponse;
public interface OrderService {

    OrderResponse placeOrder(
            String email,
            PlaceOrderRequest request);

    OrderResponse getOrderById(
            String email,
            Long orderId);

    List<OrderResponse> getMyOrders(String email);

    OrderResponse cancelOrder(
            String email,
            Long orderId);

    OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus status);
    AdminOrderDetailsResponse getAdminOrderDetails(Long orderId);
}
