
package com.salessavvy.controller;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.salessavvy.dto.WishlistResponse;
import com.salessavvy.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // Add product to wishlist
    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistResponse> addProductToWishlist(
            Authentication authentication,
            @PathVariable Long productId) {

        WishlistResponse wishlist =
                wishlistService.addProductToWishlist(
                        authentication.getName(), productId);

        return new ResponseEntity<>(
                wishlist, HttpStatus.CREATED);
    }

    // Get user's wishlist
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
            Authentication authentication) {

        return ResponseEntity.ok(
                wishlistService.getWishlist(
                        authentication.getName()));
    }

    // Remove product from wishlist
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeProductFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {

        wishlistService.removeProductFromWishlist(
                authentication.getName(), productId);

        return ResponseEntity.ok(
                "Product removed from wishlist successfully");
    }

    // Clear entire wishlist
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearWishlist(
            Authentication authentication) {

        wishlistService.clearWishlist(
                authentication.getName());

        return ResponseEntity.ok(
                "Wishlist cleared successfully");
    }
}
