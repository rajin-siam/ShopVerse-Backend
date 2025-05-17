package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductReviewDTO;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.ProductReview;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.ProductReviewRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.service.ProductReviewService;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductReviewDTO createReview(ProductReviewDTO reviewDto) {
        User currentUser = authUtil.loggedInUser();
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Check if user already reviewed this product
        reviewRepository.findByProductProductIdAndUserUserId(product.getProductId(), currentUser.getUserId())
                .ifPresent(review -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product");
                });

        ProductReview review = new ProductReview();
        review.setRating(reviewDto.getRating());
        review.setTitle(reviewDto.getTitle());
        review.setContent(reviewDto.getContent());
        review.setProduct(product);
        review.setUser(currentUser);

        ProductReview savedReview = reviewRepository.save(review);

        // Update product average rating
        updateProductRating(product);

        return modelMapper.map(savedReview, ProductReviewDTO.class);
    }


    @Override
    @Transactional
    public ProductReviewDTO updateReview(Long reviewId, ProductReviewDTO reviewDto) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        // Ensure the review belongs to the current user
        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own reviews");
        }

        review.setRating(reviewDto.getRating());
        review.setTitle(reviewDto.getTitle());
        review.setContent(reviewDto.getContent());

        ProductReview updatedReview = reviewRepository.save(review);

        // Update product average rating
        updateProductRating(review.getProduct());

        return mapToDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        //Next Feature: Ensure the review belongs to the current user or user is admin

        Product product = review.getProduct();
        reviewRepository.delete(review);

        // Update product average rating
        updateProductRating(product);
    }

    @Override
    public ProductReviewDTO getReviewById(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        User currentUser = authUtil.loggedInUser();
        ProductReviewDTO productReviewDTO = modelMapper.map(review, ProductReviewDTO.class);
        return productReviewDTO;
    }

    @Override
    public List<ProductReviewDTO> getReviewsByProductId(Long productId) {
        List<ProductReview> reviews = reviewRepository.findByProductProductId(productId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductReviewDTO> getReviewsByUserId(Long userId) {
        System.out.println(userId);
        List<ProductReview> reviews = reviewRepository.findByUserUserId(userId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ProductReviewDTO getUserReviewForProduct(Long productId) {
        User currentUser = authUtil.loggedInUser();

        ProductReview review = reviewRepository.findByProductProductIdAndUserUserId(productId, currentUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "You haven't reviewed this product yet"));

        return mapToDto(review);
    }

    private ProductReviewDTO mapToDto(ProductReview review) {
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setProductId(review.getProduct().getProductId());
        dto.setUserName(review.getUser().getUserName());
        dto.setUserEmail(review.getUser().getEmail());
        return dto;
    }

    @Transactional
    protected void updateProductRating(Product product) {
        List<ProductReview> reviews = reviewRepository.findByProductProductId(product.getProductId());

        if (reviews.isEmpty()) {
            product.setAverageRating(0);
            product.setRatingCount(0);
        } else {
            double sum = reviews.stream().mapToInt(ProductReview::getRating).sum();
            double average = sum / reviews.size();

            product.setAverageRating(average);
            product.setRatingCount(reviews.size());
        }

        productRepository.save(product);
    }


}