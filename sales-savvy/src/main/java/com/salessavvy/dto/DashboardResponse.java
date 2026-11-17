package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardResponse {

    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long lowStockProducts;
    private BigDecimal totalRevenue;

    private List<RecentOrderSummary> recentOrders;
    private List<TopSellingProductSummary> topSellingProducts;

    public DashboardResponse() {
    }

    public DashboardResponse(
            long totalUsers,
            long totalProducts,
            long totalOrders,
            long pendingOrders,
            long completedOrders,
            long lowStockProducts,
            BigDecimal totalRevenue,
            List<RecentOrderSummary> recentOrders,
            List<TopSellingProductSummary> topSellingProducts) {

        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.completedOrders = completedOrders;
        this.lowStockProducts = lowStockProducts;
        this.totalRevenue = totalRevenue;
        this.recentOrders = recentOrders;
        this.topSellingProducts = topSellingProducts;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public List<RecentOrderSummary> getRecentOrders() {
        return recentOrders;
    }

    public List<TopSellingProductSummary> getTopSellingProducts() {
        return topSellingProducts;
    }

    public static class RecentOrderSummary {

        private Long orderId;
        private String customerName;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime orderDate;

        public RecentOrderSummary(
                Long orderId,
                String customerName,
                BigDecimal totalAmount,
                String status,
                LocalDateTime orderDate) {

            this.orderId = orderId;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
        }

        public Long getOrderId() {
            return orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }
    }

    public static class TopSellingProductSummary {

        private Long productId;
        private String productName;
        private Long totalQuantitySold;

        public TopSellingProductSummary(
                Long productId,
                String productName,
                Long totalQuantitySold) {

            this.productId = productId;
            this.productName = productName;
            this.totalQuantitySold = totalQuantitySold;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Long getTotalQuantitySold() {
            return totalQuantitySold;
        }
    }
}