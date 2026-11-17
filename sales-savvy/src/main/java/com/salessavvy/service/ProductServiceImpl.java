
package com.salessavvy.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.entity.Category;
import com.salessavvy.entity.Product;
import com.salessavvy.repository.CategoryRepository;
import com.salessavvy.repository.ProductRepository;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    private void validateProduct(Product product) {

        if (product == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product details are required");
        }

        if (product.getName() == null || product.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product name is required");
        }

        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product SKU is required");
        }

        if (product.getPrice() == null
                || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product price must be greater than zero");
        }

        if (product.getStockQuantity() == null
                || product.getStockQuantity() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock quantity cannot be negative or null");
        }

        if (product.getCategory() == null
                || product.getCategory().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product category is required");
        }
    }

    private Category getActiveCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found"));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign product to an inactive category");
        }

        return category;
    }

    @Override
    public Product createProduct(Product product) {

        validateProduct(product);

        if (productRepository.existsBySku(product.getSku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product SKU already exists");
        }

        Category category = getActiveCategory(
                product.getCategory().getId());

        product.setCategory(category);
        product.setActive(true);

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {

        return productRepository.findByActiveTrue()
                .stream()
                .filter(product ->
                        product.getCategory() != null
                        && Boolean.TRUE.equals(
                                product.getCategory().getActive()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProductsForAdmin() {

        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is inactive");
        }

        if (product.getCategory() == null
                || !Boolean.TRUE.equals(
                        product.getCategory().getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product category is inactive");
        }

        return product;
    }

    @Override
    public Product updateProduct(Long id, Product updatedProduct) {

        validateProduct(updatedProduct);

        Product existingProduct = getProductById(id);

        if (!existingProduct.getSku().equals(updatedProduct.getSku())
                && productRepository.existsBySku(updatedProduct.getSku())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product SKU already exists");
        }

        Category category = getActiveCategory(
                updatedProduct.getCategory().getId());

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setCategory(category);

        return productRepository.save(existingProduct);
    }

    @Override
    public void deactivateProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found"));

        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public void activateProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found"));

        if (Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is already active");
        }

        Category category = getActiveCategory(
                product.getCategory().getId());

        product.setCategory(category);
        product.setActive(true);

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {

        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .filter(product ->
                        Boolean.TRUE.equals(product.getActive())
                        && product.getCategory() != null
                        && Boolean.TRUE.equals(
                                product.getCategory().getActive()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {

        Category category = getActiveCategory(categoryId);

        return productRepository.findByCategoryId(category.getId())
                .stream()
                .filter(product ->
                        Boolean.TRUE.equals(product.getActive()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByBrand(String brand) {

        if (brand == null || brand.isBlank()) {
            return getAllProducts();
        }

        String searchBrand = brand.trim();

        return getAllProducts()
                .stream()
                .filter(product ->
                        product.getBrand() != null
                        && product.getBrand().equalsIgnoreCase(searchBrand))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllProducts();
        }

        String search = keyword.trim().toLowerCase();

        return getAllProducts()
                .stream()
                .filter(product ->
                        (product.getName() != null
                                && product.getName().toLowerCase().contains(search))
                        ||
                        (product.getSku() != null
                                && product.getSku().toLowerCase().contains(search))
                        ||
                        (product.getBrand() != null
                                && product.getBrand().toLowerCase().contains(search))
                        ||
                        (product.getDescription() != null
                                && product.getDescription().toLowerCase().contains(search))
                )
                .toList();
    }
}
