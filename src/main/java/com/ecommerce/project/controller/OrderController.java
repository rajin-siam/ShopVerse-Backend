package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.OrderResponseDTO;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripePaymentService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/users/payments/{pgName}")
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

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable Long orderId) {
        String email = authUtil.loggedInEmail();
        OrderResponseDTO orderResponse = orderService.getOrderByIdAndEmail(orderId, email);
        return ResponseEntity.ok(orderResponse);
    }
}