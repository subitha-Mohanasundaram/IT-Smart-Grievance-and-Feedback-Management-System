package com.grievance.service;

import com.grievance.model.User;
import com.grievance.model.Role;
import com.grievance.dto.RegisterRequest;
import com.grievance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // Validate login credentials
    public Optional<User> validateLogin(String username, String password) {
        System.out.println("🔐 AuthService: Validating login for username: " + username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            System.out.println("❌ AuthService: User not found: " + username);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        System.out.println("✅ AuthService: User found: " + user.getUsername());
        
        // Check password with BCrypt
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("✅ AuthService: BCrypt match: " + passwordMatches);
        
        if (passwordMatches) {
            return Optional.of(user);
        }
        
        // For backward compatibility: check if password is stored in plain text
        if (password.equals(user.getPassword())) {
            System.out.println("⚠️ AuthService: Plain text match detected - re-encrypting...");
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return Optional.of(user);
        }
        
        System.out.println("❌ AuthService: Password doesn't match");
        return Optional.empty();
    }
    
    // Check if username exists
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    // Check if email exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // ================== REGISTRATION METHOD ==================
    // Register new user
    public User registerUser(RegisterRequest request) {
        System.out.println("📝 AuthService.registerUser() called for: " + request.getUsername());
        
        // Check if username exists
        if (usernameExists(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email exists
        if (emailExists(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // Uses Role.USER enum
        
        User savedUser = userRepository.save(user);
        System.out.println("✅ User registered successfully: " + savedUser.getUsername());
        System.out.println("✅ User ID: " + savedUser.getId());
        System.out.println("✅ Role: " + savedUser.getRole());
        return savedUser;
    }
    // =========================================================
}