package com.salessavvy.service;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salessavvy.dto.ProductSearchItemResponse;
import com.salessavvy.entity.Product;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.ProductSpecification;

@Service
@Transactional(readOnly = true)
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<ProductSearchItemResponse> searchProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction) {

        // Validate pagination
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        // Validate price range
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Maximum price cannot be negative");
        }

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum price cannot exceed maximum price"
            );
        }

        // Allow only supported sorting fields
        if (!sortBy.equals("name")
                && !sortBy.equals("price")
                && !sortBy.equals("createdAt")) {

            throw new IllegalArgumentException(
                    "Invalid sort field. Use name, price or createdAt");
        }

        // Sorting direction
        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid sorting direction. Use asc or desc");
        }

        // Create pagination and sorting
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        // Apply dynamic filters
        Page<Product> products = productRepository.findAll(
                ProductSpecification.filterProducts(
                        keyword,
                        categoryId,
                        minPrice,
                        maxPrice
                ),
                pageable
        );

        // Convert Product entities into DTOs

return products.map(product ->
        new ProductSearchItemResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory() != null
                        ? product.getCategory().getName()
                        : null
        )
);

    }
}