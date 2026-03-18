package com.grievance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MainApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
        printStartupMessage();
    }
    
    private static void printStartupMessage() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚀 IT GRIEVANCE SYSTEM BACKEND STARTED SUCCESSFULLY!");
        System.out.println("=".repeat(70));
        System.out.println("🌐 Server URL: http://localhost:8080");
        System.out.println("🔗 API Base: http://localhost:8080/api");
        System.out.println("🔐 Authentication: JWT Token Based");
        System.out.println("🔄 Scheduling: Enabled (Escalation checks every 5 minutes)");
        System.out.println("🔧 Async Processing: Enabled (For email notifications)");
        System.out.println("🔑 Default Test Credentials:");
        System.out.println("   👨‍💼 Admin: username='admin', password='admin123'");
        System.out.println("   👤 User:  username='user', password='user123'");
        System.out.println("📊 Database: MySQL it_grievance_db");
        System.out.println("⏰ Escalation: Priority-based automatic escalation");
        System.out.println("   • HIGH priority: 12 hours to Super Admin");
        System.out.println("   • MEDIUM priority: 24 hours to Super Admin");
        System.out.println("   • LOW priority: 48 hours to Super Admin");
        System.out.println("📧 Email: Simulation Mode (Set app.email.enabled=true for real emails)");
        System.out.println("=".repeat(70) + "\n");
    }
}