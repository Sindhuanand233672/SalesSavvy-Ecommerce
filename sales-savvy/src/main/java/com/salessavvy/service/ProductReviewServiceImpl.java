package com.salessavvy.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.ProductReviewRequest;
import com.salessavvy.dto.ProductReviewResponse;
import com.salessavvy.entity.Product;
import com.salessavvy.entity.ProductReview;
import com.salessavvy.entity.User;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.ProductReviewRepository;
import com.salessavvy.repository.UserRepository;

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewServiceImpl(
            ProductReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private ProductReviewResponse convertToResponse(ProductReview review) {

        ProductReviewResponse response = new ProductReviewResponse();

        response.setId(review.getId());
        response.setProductId(review.getProduct().getId());
        response.setProductName(review.getProduct().getName());
        response.setReviewerName(review.getUser().getFullName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setApproved(review.getApproved());
        response.setCreatedAt(review.getCreatedAt());

        return response;
    }

    @Override
    public ProductReviewResponse addReview(
            ProductReviewRequest request, String email) {

        User user = getUser(email);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot review an inactive product");
        }

        if (reviewRepository.existsByUser_IdAndProduct_Id(
                user.getId(), product.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already reviewed this product");
        }

        ProductReview review = new ProductReview();

        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setApproved(false);

        ProductReview savedReview = reviewRepository.save(review);

        return convertToResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewResponse> getApprovedReviews(Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found");
        }

        return reviewRepository
                .findByProduct_IdAndApprovedTrueOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewResponse> getMyReviews(String email) {

        User user = getUser(email);

        return reviewRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewResponse> getPendingReviews() {

        return reviewRepository
                .findByApprovedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public ProductReviewResponse approveReview(Long reviewId) {

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"));

        review.setApproved(true);

        ProductReview updatedReview = reviewRepository.save(review);

        return convertToResponse(updatedReview);
    }

    @Override
    public void rejectReview(Long reviewId) {

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"));

        if (Boolean.TRUE.equals(review.getApproved())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "An approved review cannot be rejected");
        }

        // Rejection removes the pending review. The existing schema has only
        // an approved flag and no rejected state/history column.
        reviewRepository.delete(review);
    }

    @Override
    public void deleteMyReview(Long reviewId, String email) {

        User user = getUser(email);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can delete only your own reviews");
        }

        reviewRepository.delete(review);
    }
}