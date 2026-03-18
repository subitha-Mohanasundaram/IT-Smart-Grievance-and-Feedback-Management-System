package com.grievance;

import com.grievance.model.Role;
import com.grievance.model.User;
import com.grievance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }
    
    private void initializeDefaultUsers() {
        System.out.println("\n" + "🔐".repeat(30));
        System.out.println("INITIALIZING DATABASE USERS");
        System.out.println("🔐".repeat(30));
        
        try {
            // Create Admin User
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setName("System Administrator");
                admin.setUsername("admin");
                admin.setEmail("admin@company.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ Created ADMIN User:");
                System.out.println("   Name: System Administrator");
                System.out.println("   Username: admin");
                System.out.println("   Password: admin123");
                System.out.println("   Role: ADMIN");
            } else {
                System.out.println("ℹ️  ADMIN user already exists");
            }
            
            // Create Regular User
            if (!userRepository.existsByUsername("user")) {
                User user = new User();
                user.setName("Regular User");
                user.setUsername("user");
                user.setEmail("user@company.com");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole(Role.USER);
                userRepository.save(user);
                System.out.println("✅ Created USER User:");
                System.out.println("   Name: Regular User");
                System.out.println("   Username: user");
                System.out.println("   Password: user123");
                System.out.println("   Role: USER");
            } else {
                System.out.println("ℹ️  USER user already exists");
            }
            
            System.out.println("✅ USER INITIALIZATION COMPLETE");
            System.out.println("🔐".repeat(30) + "\n");
            
        } catch (Exception e) {
            System.out.println("❌ Error initializing users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}