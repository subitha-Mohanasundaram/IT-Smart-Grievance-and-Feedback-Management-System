package com.grievance.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private String message;
    private boolean success;
    
    // Getters
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
    
    // Setters
    public void setToken(String token) { this.token = token; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setMessage(String message) { this.message = message; }
    public void setSuccess(boolean success) { this.success = success; }
}