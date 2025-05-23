package com.ecommerce.project.service;

import com.ecommerce.project.payload.WishlistDTO;

public interface WishlistService {
    WishlistDTO getWishlistForUser();
    WishlistDTO addProductToWishlist(Long productId);
    void removeProductFromWishlist(Long productId);
    void clearWishlist();
    boolean isProductInWishlist(Long productId);
}