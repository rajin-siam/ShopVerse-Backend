package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.payload.ProductReviewDTO;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.ProductReview;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.ProductReviewRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductReviewDTO createReview(ProductReviewDTO reviewDto) {
        User currentUser = authUtil.loggedInUser();

        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", reviewDto.getProductId()));

        // Check if user already reviewed this product
        if (reviewRepository.existsByProductProductIdAndUserUserId(product.getProductId(), currentUser.getUserId())) {
            throw new APIException("You have already reviewed this product. Use update instead.");
        }

        ProductReview review = new ProductReview();
        review.setRating(reviewDto.getRating());
        review.setTitle(reviewDto.getTitle());
        review.setContent(reviewDto.getContent());
        review.setProduct(product);
        review.setUser(currentUser);

        ProductReview savedReview = reviewRepository.save(review);

        // Update product rating statistics
        updateProductRatingStatistics(product);

        return mapToDTO(savedReview);
    }

    @Override
    public ProductReviewDTO updateReview(Long reviewId, ProductReviewDTO reviewDto) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "reviewId", reviewId));

        // Check ownership
        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new APIException("You can only update your own reviews");
        }

        review.setRating(reviewDto.getRating());
        review.setTitle(reviewDto.getTitle());
        review.setContent(reviewDto.getContent());

        ProductReview updatedReview = reviewRepository.save(review);

        // Update product rating statistics
        updateProductRatingStatistics(review.getProduct());

        return mapToDTO(updatedReview);
    }

    @Override
    public void deleteReview(Long reviewId) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "reviewId", reviewId));

        // Check ownership or admin role (you can add admin check here)
        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new APIException("You can only delete your own reviews");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Update product rating statistics
        updateProductRatingStatistics(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewDTO getReviewById(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "reviewId", reviewId));

        return mapToDTO(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewDTO> getReviewsByProductId(Long productId) {
        // Check if product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        List<ProductReview> reviews = reviewRepository.findByProductIdWithUserAndProduct(productId);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewDTO> getReviewsByUserId(Long userId) {
        List<ProductReview> reviews = reviewRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewDTO getUserReviewForProduct(Long productId) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findByProductProductIdAndUserUserId(productId, currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "productId and userId",
                        productId + " and " + currentUser.getUserId()));

        return mapToDTO(review);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long productId) {
        User currentUser = authUtil.loggedInUser();
        return reviewRepository.existsByProductProductIdAndUserUserId(productId, currentUser.getUserId());
    }

    @Override
    public void deleteUserReviewForProduct(Long productId) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findByProductProductIdAndUserUserId(productId, currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "productId and userId",
                        productId + " and " + currentUser.getUserId()));

        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Update product rating statistics
        updateProductRatingStatistics(product);
    }

    private ProductReviewDTO mapToDTO(ProductReview review) {
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setProductId(review.getProduct().getProductId());
        dto.setProductName(review.getProduct().getProductName());
        dto.setUserId(review.getUser().getUserId());
        dto.setUserName(review.getUser().getUserName());
        dto.setUserEmail(review.getUser().getEmail());
        return dto;
    }

    private void updateProductRatingStatistics(Product product) {
        Double averageRating = reviewRepository.findAverageRatingByProductId(product.getProductId());
        Long reviewCount = reviewRepository.countReviewsByProductId(product.getProductId());

        product.setAverageRating(averageRating != null ? averageRating : 0.0);
        product.setRatingCount(reviewCount != null ? reviewCount.intValue() : 0);

        productRepository.save(product);
    }
}