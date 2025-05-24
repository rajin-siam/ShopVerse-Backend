package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter @Setter  // Replace @Data with @Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
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
    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<WishlistItem> wishlistItems = new ArrayList<>();

    private double averageRating;
    private int ratingCount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductReview> reviews = new ArrayList<>();

    // Helper methods
    public void addReview(ProductReview review) {
        reviews.add(review);
        review.setProduct(this);
        updateRatingStats();
    }

    public void removeReview(ProductReview review) {
        reviews.remove(review);
        review.setProduct(null);
        updateRatingStats();
    }

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

    // Custom equals and hashCode using only ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}