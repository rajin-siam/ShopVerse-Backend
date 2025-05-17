package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotBlank
    @Size(min = 3, message = "Product Name must contain at least 3 characters")
    private String productName;

    @Lob
    private String image;

    @Lob
    @NotBlank
    @Size(min = 6, message = "Product description must contain at least 6 characters")
    private String description;

    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User user;

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<WishlistItem> wishlistItems = new ArrayList<>();

    private double averageRating;  // Summary rating
    private int ratingCount;       // Total number of ratings

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductReview> reviews = new ArrayList<>();

    // Helper method to add a review and update rating stats
    public void addReview(ProductReview review) {
        reviews.add(review);
        review.setProduct(this);
        updateRatingStats();
    }

    // Helper method to remove a review and update rating stats
    public void removeReview(ProductReview review) {
        reviews.remove(review);
        review.setProduct(null);
        updateRatingStats();
    }

    // Helper method to update rating statistics
    private void updateRatingStats() {
        if (reviews.isEmpty()) {
            this.averageRating = 0;
            this.ratingCount = 0;
        } else {
            double sum = reviews.stream().mapToInt(ProductReview::getRating).sum();
            this.averageRating = sum / reviews.size();
            this.ratingCount = reviews.size();
        }
    }
}