package com.ecommerce.project.service;

import com.ecommerce.project.payload.PaymentIntentDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class StripePaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Creates a payment intent to begin the payment process
     *
     * @param amount Amount to charge (in the smallest currency unit, e.g. cents for USD)
     * @param currency The three-letter ISO currency code (e.g. "usd")
     * @return PaymentIntentDTO with client secret for the frontend
     */
    public PaymentIntentDTO createPaymentIntent(Double amount, String currency) {
        try {
            // Convert to smallest currency unit (cents for USD)
            long amountInSmallestUnit = Math.round(amount * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(currency.toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods
                                    .builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return new PaymentIntentDTO(
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    amount,
                    currency
            );

        } catch (StripeException e) {
            throw new RuntimeException("Error creating payment intent: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a payment was successful by checking with Stripe API
     *
     * @param paymentIntentId The Stripe payment intent ID
     * @return true if payment was successful, false otherwise
     */
    public boolean verifyPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            throw new RuntimeException("Error verifying payment: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves detailed payment information from Stripe
     *
     * @param paymentIntentId The Stripe payment intent ID
     * @return The PaymentIntent object from Stripe
     */
    public PaymentIntent getPaymentDetails(String paymentIntentId) {
        try {
            PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder()
                    .addExpand("payment_method")
                    .addExpand("customer")
                    .build();

            return PaymentIntent.retrieve(paymentIntentId, params, null);
        } catch (StripeException e) {
            throw new RuntimeException("Error retrieving payment details: " + e.getMessage(), e);
        }
    }
}