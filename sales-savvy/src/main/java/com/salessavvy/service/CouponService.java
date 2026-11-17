
package com.salessavvy.service;

import java.math.BigDecimal;
import java.util.List;

import com.salessavvy.dto.CouponRequest;
import com.salessavvy.dto.CouponResponse;
import com.salessavvy.dto.CouponValidationResponse;

public interface CouponService {

    CouponResponse createCoupon(CouponRequest request);

    List<CouponResponse> getAllCoupons();

    CouponResponse getCouponByCode(String code);

    CouponResponse getCouponById(Long couponId);

    CouponResponse updateCoupon(Long couponId, CouponRequest request);

    void deactivateCoupon(Long couponId);

    void activateCoupon(Long couponId);

    CouponValidationResponse validateCoupon(
            String code, BigDecimal orderAmount);

    void recordCouponUsage(String code);
}
