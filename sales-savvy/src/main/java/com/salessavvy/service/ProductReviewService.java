package com.salessavvy.service;

import java.util.List;

import com.salessavvy.dto.ProductReviewRequest;
import com.salessavvy.dto.ProductReviewResponse;

public interface ProductReviewService {

    ProductReviewResponse addReview(
            ProductReviewRequest request, String email);

    List<ProductReviewResponse> getApprovedReviews(Long productId);

    List<ProductReviewResponse> getMyReviews(String email);

    List<ProductReviewResponse> getPendingReviews();

    ProductReviewResponse approveReview(Long reviewId);

    void rejectReview(Long reviewId);

    void deleteMyReview(Long reviewId, String email);
}