package com.ecommerce.project.repositories;

import com.ecommerce.project.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductProductIdOrderByCreatedAtDesc(Long productId);

    List<ProductReview> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProductReview> findByProductProductIdAndUserUserId(Long productId, Long userId);

    void deleteByProductProductIdAndUserUserId(Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.productId = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM ProductReview r JOIN FETCH r.user JOIN FETCH r.product WHERE r.product.productId = :productId ORDER BY r.createdAt DESC")
    List<ProductReview> findByProductIdWithUserAndProduct(@Param("productId") Long productId);

    boolean existsByProductProductIdAndUserUserId(Long productId, Long userId);
}