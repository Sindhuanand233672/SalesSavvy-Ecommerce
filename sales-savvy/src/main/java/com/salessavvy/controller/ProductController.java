
package com.salessavvy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.entity.Product;
import com.salessavvy.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        Product savedProduct = productService.createProduct(product);

        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {

        return ResponseEntity.ok(
                productService.getAllProducts());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Product>> getAllProductsForAdmin() {

        return ResponseEntity.ok(
                productService.getAllProductsForAdmin());
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Product>> getProductsByBrand(
            @PathVariable String brand) {

        return ResponseEntity.ok(
                productService.getProductsByBrand(brand));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        Product updatedProduct =
                productService.updateProduct(id, product);

        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateProduct(
            @PathVariable Long id) {

        productService.deactivateProduct(id);

        return ResponseEntity.ok(
                "Product deactivated successfully");
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateProduct(
            @PathVariable Long id) {

        productService.activateProduct(id);

        return ResponseEntity.ok(
                "Product activated successfully");
    }
}
