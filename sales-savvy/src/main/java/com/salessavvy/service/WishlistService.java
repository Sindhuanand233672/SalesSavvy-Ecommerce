
package com.salessavvy.service;

import com.salessavvy.dto.WishlistResponse;

public interface WishlistService {

    WishlistResponse addProductToWishlist(String email, Long productId);

    WishlistResponse getWishlist(String email);

    void removeProductFromWishlist(String email, Long productId);

    void clearWishlist(String email);
}
