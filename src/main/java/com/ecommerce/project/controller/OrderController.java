package com.ecommerce.project.controller;

import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripePaymentService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/order/users/payments/{pgName}")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @PathVariable String pgName,
            @RequestBody OrderRequestDTO orderRequest) {

        // Get the current user's email
        String email = authUtil.loggedInEmail();

        // Verify the payment if using Stripe
        if ("Stripe".equalsIgnoreCase(pgName)) {
            boolean paymentVerified = stripePaymentService.verifyPayment(orderRequest.getPgPaymentId());
            if (!paymentVerified) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Payment verification failed. Please try again or contact support.");
            }
        }

        // Create the order if payment is valid
        OrderResponseDTO orderResponse = orderService.createOrder(email, orderRequest);
        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/admin/order/{orderId}")
    public ResponseEntity<OrderDTO> getOrderDetails(@PathVariable Long orderId) {
        OrderDTO orderData = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderData);
    }

    // New API methods for order management

    /**
     * Get all orders with pagination, sorting, and filtering
     */
//    @GetMapping("/admin/orders")
//    public ResponseEntity<PaginatedResponse<OrderResponseDTO>> getAllOrders(
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @RequestParam(value = "limit", defaultValue = "10") int limit,
//            @RequestParam(value = "sort", defaultValue = "orderDate") String sort,
//            @RequestParam(value = "direction", defaultValue = "desc") String direction,
//            @RequestParam(value = "status", required = false) String status,
//            @RequestParam(value = "search", required = false) String search) {
//
//        // Admin access check can be added here or in the service layer
//        PaginatedResponse<OrderResponseDTO> response = orderService.getAllOrders(
//                page, limit, sort, direction, status, search);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
        @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(value = "sortBy", defaultValue = "orderDate") String sortBy,
        @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
        @RequestParam(value = "orderStatus", required = false) String orderStatus,
        @RequestParam(value = "search", required = false) String search) {
        OrderResponse orderResponse= orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder, orderStatus, search);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    /**
     * Update order status
     */

    @PatchMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestBody StatusRequest statusRequest){
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, statusRequest.getStatus());
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }


    /**
     * Update payment status
     */
    @PatchMapping("/admin/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestBody PaymentStatusUpdateRequest paymentRequest) {

        // Admin access check can be added here
        orderService.updatePaymentStatus(orderId, paymentRequest.getPaymentStatus());
        return ResponseEntity.ok(new ApiResponse("Payment status updated successfully", true));
    }


    public static class PaymentStatusUpdateRequest {
        private String paymentStatus;

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
    }
}