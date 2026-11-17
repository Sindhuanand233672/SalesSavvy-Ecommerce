package com.salessavvy.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salessavvy.dto.DashboardResponse;
import com.salessavvy.dto.DashboardResponse.RecentOrderSummary;
import com.salessavvy.dto.DashboardResponse.TopSellingProductSummary;

import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.Payment;
import com.salessavvy.entity.PaymentStatus;
import com.salessavvy.entity.Product;

import com.salessavvy.repository.UserRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.PaymentRepository;

@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public AdminDashboardServiceImpl(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public DashboardResponse getDashboardData() {

        // Fetch existing records
        var users = userRepository.findAll();
        var products = productRepository.findAll();
        var orders = orderRepository.findAll();
        var payments = paymentRepository.findAll();

        // Total active users
        long totalUsers = users.stream()
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .count();

        // Total active products
        long totalProducts = products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getActive()))
                .count();

        // Order statistics
        long totalOrders = orders.size();

        long pendingOrders = orders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.PENDING)
                .count();

        long completedOrders = orders.stream()
                .filter(order ->
                        order.getStatus() == OrderStatus.DELIVERED)
                .count();

        // Low stock products: 5 or fewer units
        long lowStockProducts = products.stream()
                .filter(product ->
                        Boolean.TRUE.equals(product.getActive()))
                .filter(product ->
                        product.getStockQuantity() != null
                        && product.getStockQuantity() <= 5)
                .count();

        // Calculate successful payment revenue
        BigDecimal totalRevenue = payments.stream()
                .filter(payment ->
                        payment.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recent five orders
        List<RecentOrderSummary> recentOrders = orders.stream()
                .sorted(Comparator.comparing(
                        Order::getOrderDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder())))
                .limit(5)
                .map(order -> new RecentOrderSummary(
                        order.getId(),
                        order.getUser() != null
                                ? order.getUser().getFullName()
                                : "Unknown",
                        order.getTotalAmount(),
                        order.getStatus().name(),
                        order.getOrderDate()))
                .collect(Collectors.toList());

        // Calculate top-selling products
        Map<Long, Long> productSales = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();

        for (Order order : orders) {

            if (order.getStatus() == OrderStatus.CANCELLED
                    || order.getStatus() == OrderStatus.REFUNDED) {
                continue;
            }

            for (OrderItem item : order.getOrderItems()) {

                Product product = item.getProduct();

                if (product == null) {
                    continue;
                }

                Long productId = product.getId();

                productSales.merge(
                        productId,
                        item.getQuantity().longValue(),
                        Long::sum
                );

                productNames.put(productId, product.getName());
            }
        }

        List<TopSellingProductSummary> topSellingProducts =
                productSales.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue()
                        .reversed())
                .limit(5)
                .map(entry -> new TopSellingProductSummary(
                        entry.getKey(),
                        productNames.get(entry.getKey()),
                        entry.getValue()))
                .collect(Collectors.toList());

        return new DashboardResponse(
                totalUsers,
                totalProducts,
                totalOrders,
                pendingOrders,
                completedOrders,
                lowStockProducts,
                totalRevenue,
                recentOrders,
                topSellingProducts
        );
    }
}