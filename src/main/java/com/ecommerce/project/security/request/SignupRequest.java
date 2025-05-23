package com.ecommerce.project.security.request;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @Size(max = 50)
    private String fullName;

    @Size(max = 20)
    @Pattern(regexp = "^\\+?[0-9\\s()-]*$") // Basic phone validation
    private String phoneNumber;
    private LocalDate dateOfBirth;

    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    public Set<String> getRole() {
        return this.role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }
}