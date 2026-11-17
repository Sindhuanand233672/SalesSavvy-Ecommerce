
package com.salessavvy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.AddToCartRequest;
import com.salessavvy.dto.CartResponse;
import com.salessavvy.service.CartService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Add product to cart
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addProductToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse cart = cartService.addProductToCart(
                authentication.getName(), request);

        return new ResponseEntity<>(
                cart, HttpStatus.CREATED);
    }

    // Get user's cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication.getName()));
    }

    // Update cart item quantity
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {

        CartResponse cart = cartService.updateCartItem(
                authentication.getName(), cartItemId, quantity);

        return ResponseEntity.ok(cart);
    }

    // Remove individual cart item
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeCartItem(
            Authentication authentication,
            @PathVariable Long cartItemId) {

        cartService.removeCartItem(
                authentication.getName(), cartItemId);

        return ResponseEntity.ok(
                "Cart item removed successfully");
    }

    // Clear entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            Authentication authentication) {

        cartService.clearCart(authentication.getName());

        return ResponseEntity.ok(
                "Cart cleared successfully");
    }
}
