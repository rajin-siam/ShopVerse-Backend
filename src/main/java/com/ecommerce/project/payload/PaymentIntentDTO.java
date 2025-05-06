package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentDTO {
    private String paymentIntentId;
    private String clientSecret;
    private Double amount;
    private String currency;
}
