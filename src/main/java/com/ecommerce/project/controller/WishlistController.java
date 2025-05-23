package com.ecommerce.project.controller;

import com.ecommerce.project.payload.ApiResponse;
import com.ecommerce.project.payload.WishlistDTO;
import com.ecommerce.project.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/public/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<WishlistDTO> getWishlist() {
        WishlistDTO wishlistDTO = wishlistService.getWishlistForUser();
        return new ResponseEntity<>(wishlistDTO, HttpStatus.OK);
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkProductInWishlist(@PathVariable Long productId) {
        boolean isInWishlist = wishlistService.isProductInWishlist(productId);
        return ResponseEntity.ok(Collections.singletonMap("isInWishlist", isInWishlist));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<WishlistDTO> addToWishlist(@PathVariable Long productId) {
        WishlistDTO updatedWishlist = wishlistService.addProductToWishlist(productId);
        return new ResponseEntity<>(updatedWishlist, HttpStatus.OK);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeProductFromWishlist(productId);
        return new ResponseEntity<>(new ApiResponse("Product removed from wishlist successfully", true), HttpStatus.OK);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearWishlist() {
        wishlistService.clearWishlist();
        return new ResponseEntity<>(new ApiResponse("Wishlist cleared successfully", true), HttpStatus.OK);
    }
}