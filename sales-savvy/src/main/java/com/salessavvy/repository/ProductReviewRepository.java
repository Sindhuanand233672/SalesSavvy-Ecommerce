package com.salessavvy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.ProductReview;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProduct_IdAndApprovedTrueOrderByCreatedAtDesc(
            Long productId);

    List<ProductReview> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<ProductReview> findByUser_IdAndProduct_Id(
            Long userId, Long productId);

    List<ProductReview> findByApprovedFalseOrderByCreatedAtDesc();

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);
}