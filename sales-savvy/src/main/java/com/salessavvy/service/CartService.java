
package com.salessavvy.service;

import com.salessavvy.dto.AddToCartRequest;
import com.salessavvy.dto.CartResponse;

public interface CartService {

    CartResponse addProductToCart(String email, AddToCartRequest request);

    CartResponse getCart(String email);

    CartResponse updateCartItem(String email, Long cartItemId, Integer quantity);

    void removeCartItem(String email, Long cartItemId);

    void clearCart(String email);
}
