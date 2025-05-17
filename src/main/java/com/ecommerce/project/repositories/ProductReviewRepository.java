package com.ecommerce.project.repositories;

import com.ecommerce.project.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductProductId(Long productId);
    List<ProductReview> findByUserUserId(Long userId);
    Optional<ProductReview> findByProductProductIdAndUserUserId(Long productId, Long userId);
    void deleteByProductProductIdAndUserUserId(Long productId, Long userId);
}