package com.ecommerce.project.service;

import com.ecommerce.project.payload.*;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderResponseDTO createOrder(String email, OrderRequestDTO orderRequest);

    OrderResponseDTO getOrderByIdAndEmail(Long orderId, String email);

   // PaginatedResponse<OrderResponseDTO> getAllOrders(int page, int limit, String sort, String direction,String status, String search);
    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String status, String search);

    OrderDTO updateOrderStatus(Long orderId, String orderStatus);

    void updatePaymentStatus(Long orderId, String paymentStatus);
}