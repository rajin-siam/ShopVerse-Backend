package com.ecommerce.project.security.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 50)
    private String fullName;

    @Size(max = 20)
    @Pattern(regexp = "^\\+?[0-9\\s()-]*$") // Basic phone validation
    private String phoneNumber;

    private LocalDate dateOfBirth;
}