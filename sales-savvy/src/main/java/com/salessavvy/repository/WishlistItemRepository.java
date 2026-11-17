package com.salessavvy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.WishlistItem;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlist_IdAndProduct_Id(
            Long wishlistId, Long productId);

}