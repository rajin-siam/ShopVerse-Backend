package com.ecommerce.project.security.request;

public class GoogleTokenRequest {
    private String token;

    public GoogleTokenRequest() {
    }

    public GoogleTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
