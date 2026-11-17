package com.salessavvy.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;

import com.salessavvy.dto.ProductSearchItemResponse;

public interface ProductSearchService {

    Page<ProductSearchItemResponse> searchProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String direction
    );
}