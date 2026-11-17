//
//package com.salessavvy.service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.server.ResponseStatusException;
//
//import com.salessavvy.dto.WishlistItemResponse;
//import com.salessavvy.dto.WishlistResponse;
//
//import com.salessavvy.entity.Product;
//import com.salessavvy.entity.User;
//import com.salessavvy.entity.Wishlist;
//import com.salessavvy.entity.WisnhlistItem;
//
//import com.salessavvy.repository.ProductRepository;
//import com.salessavvy.repository.UserRepository;
//import com.salessavvy.repository.WishlistRepository;
//
//@Service
//@Transactional
//public class WishlistServiceImpl implements WishlistService {
//
//    private final WishlistRepository wishlistRepository;
//    private final ProductRepository productRepository;
//    private final UserRepository userRepository;
//
//    public WishlistServiceImpl(
//            WishlistRepository wishlistRepository,
//            ProductRepository productRepository,
//            UserRepository userRepository) {
//
//        this.wishlistRepository = wishlistRepository;
//        this.productRepository = productRepository;
//        this.userRepository = userRepository;
//    }
//
//    // Get user using email
//    private User getUser(String email) {
//
//        return userRepository.findByEmail(email)
//                .orElseThrow(() ->
//                    new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "User not found"));
//    }
//
//    // Get existing wishlist or create a new one
//    private Wishlist getOrCreateWishlist(User user) {
//
//        return wishlistRepository.findByUser_Id(user.getId())
//                .orElseGet(() -> {
//
//                    Wishlist wishlist = new Wishlist();
//                    wishlist.setUser(user);
//
//                    return wishlistRepository.save(wishlist);
//                });
//    }
//
//    // Convert Wishlist entity into WishlistResponse DTO
//    private WishlistResponse convertToResponse(Wishlist wishlist) {
//
//        List<WishlistItemResponse> items = wishlist.getItems()
//                .stream()
//                .map(item -> new WishlistItemResponse(
//                        item.getId(),
//                        item.getProduct().getId(),
//                        item.getProduct().getName(),
//                        item.getProduct().getSku(),
//                        item.getProduct().getPrice(),
//                        item.getProduct().getImageUrl()
//                ))
//                .collect(Collectors.toList());
//
//        return new WishlistResponse(
//                wishlist.getId(),
//                wishlist.getCreatedAt(),
//                items
//        );
//    }
//
//    // Add product to wishlist
//    @Override
//    public WishlistResponse addProductToWishlist(
//            String email, Long productId) {
//
//        User user = getUser(email);
//
//        Wishlist wishlist = getOrCreateWishlist(user);
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() ->
//                    new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Product not found"));
//
//        if (!Boolean.TRUE.equals(product.getActive())) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Product is inactive");
//        }
//
//        boolean alreadyExists = wishlist.getItems().stream()
//                .anyMatch(item ->
//                    item.getProduct().getId().equals(productId));
//
//        if (alreadyExists) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Product already exists in wishlist");
//        }
//
//        WishlistItem item = new WishlistItem();
//
//        item.setWishlist(wishlist);
//        item.setProduct(product);
//
//        wishlist.getItems().add(item);
//
//        Wishlist savedWishlist = wishlistRepository.save(wishlist);
//
//        return convertToResponse(savedWishlist);
//    }
//
//    // Get user's wishlist
//    @Override
//    @Transactional(readOnly = true)
//    public WishlistResponse getWishlist(String email) {
//
//        User user = getUser(email);
//
//        return wishlistRepository.findByUser_Id(user.getId())
//                .map(this::convertToResponse)
//                .orElseGet(() -> new WishlistResponse(
//                        null,
//                        null,
//                        List.of()
//                ));
//    }
//
//    // Remove product from wishlist
//    @Override
//    public void removeProductFromWishlist(
//            String email, Long productId) {
//
//        User user = getUser(email);
//
//        Wishlist wishlist = getOrCreateWishlist(user);
//
//        WishlistItem item = wishlist.getItems().stream()
//                .filter(wishlistItem ->
//                    wishlistItem.getProduct().getId().equals(productId))
//                .findFirst()
//                .orElseThrow(() ->
//                    new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Product not found in wishlist"));
//
//        wishlist.getItems().remove(item);
//
//        wishlistRepository.save(wishlist);
//    }
//
//    // Clear entire wishlist
//    @Override
//    public void clearWishlist(String email) {
//
//        User user = getUser(email);
//
//        Wishlist wishlist = getOrCreateWishlist(user);
//
//        wishlist.getItems().clear();
//
//        wishlistRepository.save(wishlist);
//    }
//}




package com.salessavvy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.salessavvy.dto.WishlistItemResponse;
import com.salessavvy.dto.WishlistResponse;

import com.salessavvy.entity.Product;
import com.salessavvy.entity.User;
import com.salessavvy.entity.Wishlist;
import com.salessavvy.entity.WishlistItem;

import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.UserRepository;
import com.salessavvy.repository.WishlistRepository;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistServiceImpl(
            WishlistRepository wishlistRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.wishlistRepository = wishlistRepository;
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

    // Get existing wishlist or create a new one
    private Wishlist getOrCreateWishlist(User user) {

        return wishlistRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {

                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);

                    return wishlistRepository.save(wishlist);
                });
    }

    // Convert Wishlist entity into WishlistResponse DTO
    private WishlistResponse convertToResponse(Wishlist wishlist) {

        List<WishlistItemResponse> items = wishlist.getItems()
                .stream()
                .map(item -> new WishlistItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getProduct().getPrice(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getStockQuantity()
                ))
                .collect(Collectors.toList());

        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getCreatedAt(),
                items
        );
    }

    // Add product to wishlist
    @Override
    public WishlistResponse addProductToWishlist(
            String email, Long productId) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is inactive");
        }

        boolean alreadyExists = wishlist.getItems().stream()
                .anyMatch(item ->
                    item.getProduct().getId().equals(productId));

        if (alreadyExists) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product already exists in wishlist");
        }

        WishlistItem item = new WishlistItem();

        item.setWishlist(wishlist);
        item.setProduct(product);

        wishlist.getItems().add(item);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        return convertToResponse(savedWishlist);
    }

    // Get user's wishlist
    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String email) {

        User user = getUser(email);

        return wishlistRepository.findByUser_Id(user.getId())
                .map(this::convertToResponse)
                .orElseGet(() -> new WishlistResponse(
                        null,
                        null,
                        List.of()
                ));
    }

    // Remove product from wishlist
    @Override
    public void removeProductFromWishlist(
            String email, Long productId) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        WishlistItem item = wishlist.getItems().stream()
                .filter(wishlistItem ->
                    wishlistItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in wishlist"));

        wishlist.getItems().remove(item);

        wishlistRepository.save(wishlist);
    }

    // Clear entire wishlist
    @Override
    public void clearWishlist(String email) {

        User user = getUser(email);

        Wishlist wishlist = getOrCreateWishlist(user);

        wishlist.getItems().clear();

        wishlistRepository.save(wishlist);
    }
}
