package com.ecommerce.project.controller;

import com.ecommerce.project.payload.PaymentIntentDTO;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.service.StripePaymentService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class StripeController {

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntentDTO> createPaymentIntent(
            @RequestParam(required = false) String currency) {

        String email = authUtil.loggedInEmail();
        Double cartTotal = cartService.getCartByEmail(email).getTotalPrice();

        if (currency == null) {
            currency = "usd";
        }

        PaymentIntentDTO paymentIntent = stripePaymentService.createPaymentIntent(cartTotal, currency);
        return ResponseEntity.ok(paymentIntent);
    }
}
