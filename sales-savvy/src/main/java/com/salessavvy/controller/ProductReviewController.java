package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.ProductReviewRequest;
import com.salessavvy.dto.ProductReviewResponse;
import com.salessavvy.service.ProductReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    public ProductReviewController(ProductReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Customer adds a review
    @PostMapping
    public ResponseEntity<ProductReviewResponse> addReview(
            @Valid @RequestBody ProductReviewRequest request,
            Authentication authentication) {

        ProductReviewResponse response = reviewService.addReview(
                request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Anyone can view approved reviews of a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewResponse>> getApprovedReviews(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                reviewService.getApprovedReviews(productId));
    }

    // Customer views their own reviews
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ProductReviewResponse>> getMyReviews(
            Authentication authentication) {

        return ResponseEntity.ok(
                reviewService.getMyReviews(authentication.getName()));
    }

    // Admin views pending reviews
    @GetMapping("/admin/pending")
    public ResponseEntity<List<ProductReviewResponse>> getPendingReviews() {

        return ResponseEntity.ok(reviewService.getPendingReviews());
    }

    // Admin approves a review
    @PutMapping("/admin/{reviewId}/approve")
    public ResponseEntity<ProductReviewResponse> approveReview(
            @PathVariable Long reviewId) {

        return ResponseEntity.ok(
                reviewService.approveReview(reviewId));
    }

    // Admin rejects a pending review (removed because the current schema has no rejected status)
    @DeleteMapping("/admin/{reviewId}/reject")
    public ResponseEntity<String> rejectReview(@PathVariable Long reviewId) {
        reviewService.rejectReview(reviewId);
        return ResponseEntity.ok("Review rejected successfully");
    }

    // Customer deletes their own review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteMyReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        reviewService.deleteMyReview(
                reviewId, authentication.getName());

        return ResponseEntity.ok("Review deleted successfully");
    }
}