
package com.salessavvy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salessavvy.entity.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUser_Id(Long userId);

}
