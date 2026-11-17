
package com.salessavvy.service;

import java.util.List;

import com.salessavvy.entity.Product;

public interface ProductService {

    Product createProduct(Product product);

    List<Product> getAllProducts();

    List<Product> getAllProductsForAdmin();

    Product getProductById(Long id);

    Product updateProduct(Long id, Product product);

    void deactivateProduct(Long id);

    void activateProduct(Long id);

    List<Product> searchProductsByName(String name);

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> getProductsByBrand(String brand);

    List<Product> searchProducts(String keyword);
}
