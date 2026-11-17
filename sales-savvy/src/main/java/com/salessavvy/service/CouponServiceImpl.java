
package com.salessavvy.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.CouponRequest;
import com.salessavvy.dto.CouponResponse;
import com.salessavvy.dto.CouponValidationResponse;
import com.salessavvy.entity.Coupon;
import com.salessavvy.entity.DiscountType;
import com.salessavvy.repository.CouponRepository;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    // Create coupon
    @Override
    public CouponResponse createCoupon(CouponRequest request) {

        validateRequest(request);

        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Coupon code already exists");
        }

        Coupon coupon = new Coupon();

        copyRequestToEntity(request, coupon);

        coupon.setCode(code);
        coupon.setUsageCount(0);
        coupon.setActive(true);

        return convertToResponse(couponRepository.save(coupon));
    }

    // Get all coupons
    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {

        return couponRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get coupon by code
    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String code) {

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon code is required");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found"));

        return convertToResponse(coupon);
    }

    // Get coupon by ID
    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long couponId) {

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found"));

        return convertToResponse(coupon);
    }

    // Update coupon
    @Override
    public CouponResponse updateCoupon(
            Long couponId, CouponRequest request) {

        validateRequest(request);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found"));

        String code = request.getCode().trim().toUpperCase();

        couponRepository.findByCodeIgnoreCase(code)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(couponId)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Coupon code already exists");
                    }
                });

        copyRequestToEntity(request, coupon);
        coupon.setCode(code);

        return convertToResponse(couponRepository.save(coupon));
    }

    // Deactivate coupon
    @Override
    public void deactivateCoupon(Long couponId) {

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found"));

        coupon.setActive(false);

        couponRepository.save(coupon);
    }

    // Activate coupon
    @Override
    public void activateCoupon(Long couponId) {

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Coupon not found"));

        coupon.setActive(true);

        couponRepository.save(coupon);
    }

    // Validate coupon and calculate discount
    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(
            String code, BigDecimal orderAmount) {

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon code is required");
        }

        if (orderAmount == null
                || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order amount");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Invalid coupon code"));

        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is inactive");
        }

        if (now.isBefore(coupon.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is not active yet");
        }

        if (now.isAfter(coupon.getExpiryDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon has expired");
        }

        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon usage limit reached");
        }

        if (orderAmount.compareTo(
                coupon.getMinimumOrderAmount()) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum order amount not reached");
        }

        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {

            discount = orderAmount
                    .multiply(coupon.getDiscountValue())
                    .divide(
                            new BigDecimal("100"),
                            2,
                            RoundingMode.HALF_UP);

            if (coupon.getMaximumDiscountAmount() != null
                    && discount.compareTo(
                            coupon.getMaximumDiscountAmount()) > 0) {

                discount = coupon.getMaximumDiscountAmount();
            }

        } else {

            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        discount = discount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalAmount = orderAmount.subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);

        return new CouponValidationResponse(
                coupon.getCode(),
                true,
                "Coupon applied successfully",
                orderAmount,
                discount,
                finalAmount);
    }

    // Record coupon usage after successful payment
    @Override
    public void recordCouponUsage(String code) {

        if (code == null || code.isBlank()) {
            return;
        }

        String normalizedCode = code.trim();

        int updatedRows = couponRepository.incrementUsageIfAvailable(
                normalizedCode,
                LocalDateTime.now());

        if (updatedRows == 0) {

            Coupon coupon = couponRepository
                    .findByCodeIgnoreCase(normalizedCode)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Coupon not found"));

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon is inactive, expired, or usage limit reached");
        }
    }

    // Validate coupon request
    private void validateRequest(CouponRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon request is required");
        }

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon code is required");
        }

        if (request.getDescription() == null
                || request.getDescription().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Coupon description is required");
        }

        if (request.getDiscountType() == null
                || request.getDiscountValue() == null
                || request.getMinimumOrderAmount() == null
                || request.getUsageLimit() == null
                || request.getStartDate() == null
                || request.getExpiryDate() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required coupon fields cannot be null");
        }

        if (!request.getExpiryDate().isAfter(
                request.getStartDate())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiry date must be after start date");
        }

        if (request.getDiscountValue()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Discount value must be positive");
        }

        if (request.getDiscountType() == DiscountType.PERCENTAGE
                && request.getDiscountValue()
                        .compareTo(new BigDecimal("100")) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Percentage discount cannot exceed 100");
        }

        if (request.getMinimumOrderAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum order amount cannot be negative");
        }

        if (request.getMaximumDiscountAmount() != null
                && request.getMaximumDiscountAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum discount must be positive");
        }

        if (request.getUsageLimit() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usage limit must be at least 1");
        }
    }

    // Copy request values to entity
    private void copyRequestToEntity(
            CouponRequest request, Coupon coupon) {

        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumOrderAmount(
                request.getMinimumOrderAmount());
        coupon.setMaximumDiscountAmount(
                request.getMaximumDiscountAmount());
        coupon.setStartDate(request.getStartDate());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setUsageLimit(request.getUsageLimit());
    }

    // Convert entity to response DTO
    private CouponResponse convertToResponse(Coupon coupon) {

        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinimumOrderAmount(),
                coupon.getMaximumDiscountAmount(),
                coupon.getStartDate(),
                coupon.getExpiryDate(),
                coupon.getUsageLimit(),
                coupon.getUsageCount(),
                coupon.getActive(),
                coupon.getCreatedAt());
    }
}
