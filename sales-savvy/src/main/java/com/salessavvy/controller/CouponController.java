
package com.salessavvy.controller;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.salessavvy.dto.CouponRequest;
import com.salessavvy.dto.CouponResponse;
import com.salessavvy.dto.CouponValidationRequest;
import com.salessavvy.dto.CouponValidationResponse;
import com.salessavvy.service.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // ADMIN: Create coupon
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CouponRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(couponService.createCoupon(request));
    }

    // ADMIN: Get all coupons
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {

        return ResponseEntity.ok(couponService.getAllCoupons());
    }
 // PUBLIC: Get coupons that customers can currently use
    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getActiveCoupons() {

        LocalDateTime now = LocalDateTime.now();

        List<CouponResponse> activeCoupons = couponService.getAllCoupons()
                .stream()
                .filter(coupon -> Boolean.TRUE.equals(coupon.getActive()))
                .filter(coupon -> coupon.getStartDate() == null
                        || !coupon.getStartDate().isAfter(now))
                .filter(coupon -> coupon.getExpiryDate() == null
                        || !coupon.getExpiryDate().isBefore(now))
                .filter(coupon -> coupon.getUsageLimit() == null
                        || coupon.getUsageCount() == null
                        || coupon.getUsageCount() < coupon.getUsageLimit())
                .toList();

        return ResponseEntity.ok(activeCoupons);
    }

    // ADMIN: Get coupon by code
    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(
            @PathVariable String code) {

        return ResponseEntity.ok(
                couponService.getCouponByCode(code));
    }

    // ADMIN: Get coupon by ID
    @GetMapping("/id/{couponId}")
    public ResponseEntity<CouponResponse> getCouponById(
            @PathVariable Long couponId) {

        return ResponseEntity.ok(
                couponService.getCouponById(couponId));
    }

    // ADMIN: Update coupon
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponRequest request) {

        return ResponseEntity.ok(
                couponService.updateCoupon(couponId, request));
    }

    // ADMIN: Deactivate coupon
    @DeleteMapping("/{couponId}")
    public ResponseEntity<String> deactivateCoupon(
            @PathVariable Long couponId) {

        couponService.deactivateCoupon(couponId);

        return ResponseEntity.ok(
                "Coupon deactivated successfully");
    }
 // ADMIN: Activate coupon
    @PutMapping("/{couponId}/activate")
    public ResponseEntity<String> activateCoupon(
            @PathVariable Long couponId) {

        couponService.activateCoupon(couponId);

        return ResponseEntity.ok("Coupon activated successfully");
    }

    // CUSTOMER: Validate coupon
    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody CouponValidationRequest request) {

        return ResponseEntity.ok(
                couponService.validateCoupon(
                        request.getCode(),
                        request.getOrderAmount()));
    }
}
