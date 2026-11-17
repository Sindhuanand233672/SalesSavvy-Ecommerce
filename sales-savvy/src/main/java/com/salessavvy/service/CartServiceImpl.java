
package com.salessavvy.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.AddToCartRequest;
import com.salessavvy.dto.CartItemResponse;
import com.salessavvy.dto.CartResponse;

import com.salessavvy.entity.Cart;
import com.salessavvy.entity.CartItem;
import com.salessavvy.entity.Product;
import com.salessavvy.entity.User;

import com.salessavvy.repository.CartRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.UserRepository;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Get user using email
    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }

    // Get existing cart or create a new one
    private Cart getOrCreateCart(User user) {

        return cartRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {

                    Cart cart = new Cart();
                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }

    // Validate product availability
    private void validateProduct(Product product) {

        if (!Boolean.TRUE.equals(product.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is inactive: " + product.getName());
        }

        if (product.getCategory() == null
                || !Boolean.TRUE.equals(
                        product.getCategory().getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product category is inactive: "
                            + product.getName());
        }

        if (product.getStockQuantity() == null
                || product.getStockQuantity() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is out of stock: " + product.getName());
        }
    }

    // Convert Cart entity into CartResponse DTO
    private CartResponse convertToResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> {

                    Product product = item.getProduct();

                    BigDecimal subtotal = product.getPrice()
                            .multiply(BigDecimal.valueOf(
                                    item.getQuantity()));

                    return new CartItemResponse(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            product.getPrice(),
                            product.getImageUrl(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .collect(Collectors.toList());

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getCreatedAt(),
                items,
                totalItems,
                totalAmount
        );
    }

    // Add product to cart
    @Override
    public CartResponse addProductToCart(
            String email, AddToCartRequest request) {

        if (request == null
                || request.getProductId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID is required");
        }

        if (request.getQuantity() == null
                || request.getQuantity() < 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1");
        }

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(
                request.getProductId())
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        // Validate product and category availability
        validateProduct(product);

        int requestedQuantity = request.getQuantity();

        if (requestedQuantity > product.getStockQuantity()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient stock available");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item ->
                    item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity() + requestedQuantity;

            if (newQuantity > product.getStockQuantity()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Requested quantity exceeds available stock");
            }

            existingItem.setQuantity(newQuantity);

        } else {

            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(requestedQuantity);

            cart.getItems().add(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);

        return convertToResponse(savedCart);
    }

    // Get user's cart
    @Override
    public CartResponse getCart(String email) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        return convertToResponse(cart);
    }

    // Update cart item quantity
    @Override
    public CartResponse updateCartItem(
            String email, Long cartItemId, Integer quantity) {

        if (quantity == null || quantity < 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1");
        }

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        CartItem item = cart.getItems().stream()
                .filter(cartItem ->
                    cartItem.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"));

        Product product = item.getProduct();

        // Validate product and category availability
        validateProduct(product);

        if (quantity > product.getStockQuantity()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient stock available");
        }

        item.setQuantity(quantity);

        Cart savedCart = cartRepository.save(cart);

        return convertToResponse(savedCart);
    }

    // Remove individual cart item
    @Override
    public void removeCartItem(
            String email, Long cartItemId) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        CartItem item = cart.getItems().stream()
                .filter(cartItem ->
                    cartItem.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"));

        cart.getItems().remove(item);

        cartRepository.save(cart);
    }

    // Clear entire cart
    @Override
    public void clearCart(String email) {

        User user = getUser(email);

        Cart cart = getOrCreateCart(user);

        cart.getItems().clear();

        cartRepository.save(cart);
    }
}
