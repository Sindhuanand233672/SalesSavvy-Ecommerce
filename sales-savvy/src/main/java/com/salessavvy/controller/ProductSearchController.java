package com.salessavvy.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.ProductSearchItemResponse;
import com.salessavvy.service.ProductSearchService;

@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    public ProductSearchController(
            ProductSearchService productSearchService) {

        this.productSearchService = productSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductSearchItemResponse>> searchProducts(

            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) Long categoryId,

            @RequestParam(required = false) BigDecimal minPrice,

            @RequestParam(required = false) BigDecimal maxPrice,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "createdAt") String sortBy,

            @RequestParam(defaultValue = "desc") String direction) {

        Page<ProductSearchItemResponse> response =
                productSearchService.searchProducts(
                        keyword,
                        categoryId,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(response);
    }
}