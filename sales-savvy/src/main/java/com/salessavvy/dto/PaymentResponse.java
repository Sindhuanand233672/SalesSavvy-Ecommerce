
package com.salessavvy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.salessavvy.entity.PaymentMethod;
import com.salessavvy.entity.PaymentStatus;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpayKeyId;

    public PaymentResponse() {
    }

    public PaymentResponse(
            Long id,
            Long orderId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            String transactionId,
            LocalDateTime paymentDate,
            LocalDateTime createdAt) {

        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

	public String getRazorpayOrderId() {
		return razorpayOrderId;
	}

	public void setRazorpayOrderId(String razorpayOrderId) {
		this.razorpayOrderId = razorpayOrderId;
	}

	public String getRazorpayPaymentId() {
		return razorpayPaymentId;
	}

	public void setRazorpayPaymentId(String razorpayPaymentId) {
		this.razorpayPaymentId = razorpayPaymentId;
	}

	public String getRazorpayKeyId() {
		return razorpayKeyId;
	}

	public void setRazorpayKeyId(String razorpayKeyId) {
		this.razorpayKeyId = razorpayKeyId;
	}
    
}
