package com.ecommerce.project.controller;

import com.ecommerce.project.payload.ProductReviewDTO;
import com.ecommerce.project.service.ProductReviewService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    @PostMapping
    public ResponseEntity<ProductReviewDTO> createReview(@Valid @RequestBody ProductReviewDTO reviewDto) {
        System.out.println(reviewDto.toString());
        ProductReviewDTO createdReview = reviewService.createReview(reviewDto);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ProductReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ProductReviewDTO reviewDto) {
        ProductReviewDTO updatedReview = reviewService.updateReview(reviewId, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ProductReviewDTO> getReviewById(@PathVariable Long reviewId) {
        ProductReviewDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewDTO>> getReviewsByProductId(@PathVariable Long productId) {
        List<ProductReviewDTO> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductReviewDTO>> getReviewsByUserId(@PathVariable Long userId) {
        List<ProductReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my-review/product/{productId}")
    public ResponseEntity<ProductReviewDTO> getUserReviewForProduct(@PathVariable Long productId) {
        ProductReviewDTO review = reviewService.getUserReviewForProduct(productId);
        return ResponseEntity.ok(review);
    }



}