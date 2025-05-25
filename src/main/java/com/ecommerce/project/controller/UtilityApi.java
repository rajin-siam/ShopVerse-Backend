package com.ecommerce.project.controller;

import com.ecommerce.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/utility")
public class UtilityApi {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Object>> getUserCount() {
        try {
            long userCount = userRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", userCount);
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to retrieve user count");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}