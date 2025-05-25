package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductReviewDTO;
import java.util.List;

public interface ProductReviewService {
    ProductReviewDTO createReview(ProductReviewDTO reviewDto);
    ProductReviewDTO updateReview(Long reviewId, ProductReviewDTO reviewDto);
    void deleteReview(Long reviewId);
    ProductReviewDTO getReviewById(Long reviewId);
    List<ProductReviewDTO> getReviewsByProductId(Long productId);
    List<ProductReviewDTO> getReviewsByUserId(Long userId);
    ProductReviewDTO getUserReviewForProduct(Long productId);
    boolean hasUserReviewedProduct(Long productId);
    void deleteUserReviewForProduct(Long productId);
}