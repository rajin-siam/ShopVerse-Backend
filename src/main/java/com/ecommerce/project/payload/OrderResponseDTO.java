package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentId;
    private AddressDTO shippingAddress;
    private List<OrderItemDTO> orderItems;

    // Nested DTO for order items
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
    }

    // Nested DTO for address
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private Long addressId;
        private String street;
        private String city;
        private String state;
        private String country;
        private String pincode;
        private String buildingName;
    }
}