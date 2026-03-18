package com.grievance.service;

import com.grievance.model.Complaint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:no-reply@grievance.com}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    public EmailService() {
        System.out.println("📧 EmailService initialized");
        System.out.println("   Email enabled: " + emailEnabled);
        System.out.println("   From email: " + fromEmail);
    }
    
    // ========== ADD THIS NEW METHOD ==========
    @Async
    public void sendComplaintNotification(Complaint complaint) {
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 [SIMULATED] Complaint created: ID=" + complaint.getId() + 
                             ", Title=" + complaint.getTitle() + 
                             ", User=" + (complaint.getUser() != null ? complaint.getUser().getEmail() : "Unknown"));
            return;
        }
        
        try {
            String userEmail = complaint.getUser() != null ? complaint.getUser().getEmail() : "no-email";
            String subject = "✅ Complaint #" + complaint.getId() + " Created Successfully";
            
            String body = String.format(
                "Dear User,\n\n" +
                "Your complaint has been successfully created.\n\n" +
                "📋 Complaint Details:\n" +
                "   • Complaint ID: #%d\n" +
                "   • Title: %s\n" +
                "   • Category: %s\n" +
                "   • Priority: %s\n" +
                "   • Status: %s\n" +
                "   • Created on: %s\n\n" +
                "You can track the status of your complaint in your dashboard.\n" +
                "Our support team will review it shortly.\n\n" +
                "Thank you for using our IT Grievance System.\n\n" +
                "Best regards,\n" +
                "IT Grievance System Team\n" +
                "----------------------------\n" +
                "This is an automated notification.",
                complaint.getId(), 
                complaint.getTitle(), 
                complaint.getCategory(),
                complaint.getPriority().name(),
                complaint.getStatus().name(),
                complaint.getCreatedAt()
            );
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Complaint creation email sent to: " + userEmail);
        } catch (Exception e) {
            System.out.println("❌ Failed to send complaint creation email: " + e.getMessage());
        }
    }
    // ========== END OF NEW METHOD ==========
    
    @Async
    public void sendEscalationEmail(String toEmail, String subject, String body) {
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 [SIMULATED] Email would be sent to: " + toEmail);
            System.out.println("   Subject: " + subject);
            System.out.println("   Body: " + body.substring(0, Math.min(100, body.length())) + "...");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Email sent to: " + toEmail);
        } catch (Exception e) {
            System.out.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
            System.out.println("📧 [FALLBACK] Email content for " + toEmail + ":");
            System.out.println("   Subject: " + subject);
            System.out.println("   Body: " + body);
        }
    }
    
    @Async
    public void sendUserEscalationNotification(String userEmail, Long complaintId, 
                                              String complaintTitle, Integer escalationLevel,
                                              String priority) {
        String subject = "🚨 Your " + priority + " Priority Complaint #" + complaintId + " Has Been Escalated!";
        
        String timeLimit = getTimeLimitByPriority(priority);
        
        String body = String.format(
            "Dear User,\n\n" +
            "Your %s priority complaint has been escalated to SUPER ADMIN for immediate attention.\n\n" +
            "📋 Complaint Details:\n" +
            "   • Complaint ID: #%d\n" +
            "   • Title: %s\n" +
            "   • Priority: %s\n" +
            "   • Escalation Reason: Not resolved by Admin within %s\n" +
            "   • Escalated to: SUPER ADMIN\n\n" +
            "Our SUPER ADMIN team has been notified and will work on resolving your issue immediately.\n" +
            "You will receive another update when your complaint is resolved.\n\n" +
            "Thank you for your patience.\n\n" +
            "Best regards,\n" +
            "IT Grievance System Team\n" +
            "----------------------------\n" +
            "This is an automated notification. Please do not reply to this email.",
            priority, complaintId, complaintTitle, priority, timeLimit
        );
        
        sendEscalationEmail(userEmail, subject, body);
    }
    
    @Async
    public void sendSuperAdminEscalationNotification(String superAdminEmail, Long complaintId, 
                                                    String complaintTitle, String userName, 
                                                    Integer currentLevel, String priority) {
        String subject = "🔥 " + priority + " PRIORITY - Complaint #" + complaintId + " Escalated to SUPER ADMIN!";
        
        String timeLimit = getTimeLimitByPriority(priority);
        
        String body = String.format(
            "URGENT ATTENTION REQUIRED - %s PRIORITY COMPLAINT ESCALATED TO SUPER ADMIN!\n\n" +
            "⚠️ COMPLAINT DETAILS:\n" +
            "   • Complaint ID: #%d\n" +
            "   • Title: %s\n" +
            "   • Submitted by: %s\n" +
            "   • Priority: %s\n" +
            "   • Escalation Level: %d (SUPER ADMIN)\n" +
            "   • Status: PENDING - REQUIRES IMMEDIATE ACTION\n\n" +
            "⏰ ESCALATION TIMELINE:\n" +
            "   - Created: User submitted complaint\n" +
            "   - Admin assigned: Attempted to resolve\n" +
            "   - %s priority time limit: %s elapsed\n" +
            "   - NOW: Escalated to SUPER ADMIN (Level 1)\n\n" +
            "🚨 ACTION REQUIRED:\n" +
            "1. Review complaint details immediately\n" +
            "2. Assign to appropriate personnel\n" +
            "3. Resolve as per priority level\n" +
            "4. Update complaint status\n\n" +
            "The user has been notified about this escalation.\n" +
            "Please ensure prompt resolution to maintain system credibility.\n\n" +
            "Best regards,\n" +
            "IT Grievance System - Auto Escalation System",
            priority, complaintId, complaintTitle, userName, priority, currentLevel,
            priority, timeLimit
        );
        
        sendEscalationEmail(superAdminEmail, subject, body);
    }
    
    @Async
    public void sendAdminEscalationNotification(String adminEmail, Long complaintId, 
                                               String complaintTitle, String userName, 
                                               Integer escalationLevel, String assigneeRole) {
        String subject = "⚠️ Complaint #" + complaintId + " Escalated to Level " + escalationLevel;
        
        String body = String.format(
            "Complaint Escalation Notification\n\n" +
            "📋 Complaint Details:\n" +
            "   • ID: #%d\n" +
            "   • Title: %s\n" +
            "   • User: %s\n" +
            "   • Escalation Level: %d\n" +
            "   • Assigned to: %s\n\n" +
            "This complaint has been automatically escalated due to:\n" +
            "   - Not being resolved within the specified time limit\n" +
            "   - Requiring higher authority attention\n\n" +
            "📋 Required Actions:\n" +
            "1. Review the complaint details\n" +
            "2. Take appropriate action\n" +
            "3. Update the complaint status\n" +
            "4. Add resolution notes\n\n" +
            "Please address this complaint promptly.\n\n" +
            "Best regards,\n" +
            "IT Grievance System",
            complaintId, complaintTitle, userName, escalationLevel, assigneeRole
        );
        
        sendEscalationEmail(adminEmail, subject, body);
    }
    
    @Async
    public void sendComplaintResolvedNotification(String userEmail, Long complaintId, 
                                                 String complaintTitle, String resolvedBy) {
        String subject = "✅ Your Complaint #" + complaintId + " Has Been Resolved!";
        
        String body = String.format(
            "Dear User,\n\n" +
            "Great news! Your complaint has been successfully resolved.\n\n" +
            "📋 Resolution Details:\n" +
            "   • Complaint ID: #%d\n" +
            "   • Title: %s\n" +
            "   • Resolved by: %s\n" +
            "   • Resolved on: %s\n\n" +
            "Thank you for using our IT Grievance System.\n" +
            "If you have any further issues, please submit a new complaint.\n\n" +
            "Best regards,\n" +
            "IT Grievance System Team\n" +
            "----------------------------\n" +
            "This is an automated notification. Please do not reply to this email.",
            complaintId, complaintTitle, resolvedBy, new java.util.Date().toString()
        );
        
        sendEscalationEmail(userEmail, subject, body);
    }
    
    @Async
    public void sendCommentNotification(String toEmail, String subject, String body) {
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 [SIMULATED] Comment email would be sent to: " + toEmail);
            System.out.println("   Subject: " + subject);
            System.out.println("   Body preview: " + (body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body));
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Comment email sent to: " + toEmail);
        } catch (Exception e) {
            System.out.println("❌ Failed to send comment email: " + e.getMessage());
            System.out.println("📧 [FALLBACK] Comment email for " + toEmail + ":");
            System.out.println("   Subject: " + subject);
            System.out.println("   Body: " + body);
        }
    }
    
    @Async
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        sendCommentNotification(toEmail, subject, body);
    }
    
    @Async
    public void sendAnalyticsReportEmail(String toEmail, String reportData) {
        String subject = "📊 IT Grievance System - Analytics Report";
        
        String body = String.format(
            "IT Grievance System Analytics Report\n\n" +
            "Attached is your requested analytics report.\n\n" +
            "Report generated on: %s\n\n" +
            "Summary data:\n%s\n\n" +
            "Please find the detailed report in the attachment.\n\n" +
            "Best regards,\n" +
            "IT Grievance System Analytics Team",
            new java.util.Date().toString(),
            reportData
        );
        
        sendEscalationEmail(toEmail, subject, body);
    }
    
    @Async
    public void sendDailySummaryEmail(String adminEmail, long newComplaints, long resolvedToday, 
                                     long pendingEscalations, String topCategories) {
        String subject = "📋 IT Grievance System - Daily Summary (" + new java.util.Date().toString() + ")";
        
        String body = String.format(
            "Daily System Summary Report\n\n" +
            "📊 Today's Statistics:\n" +
            "   • New Complaints Today: %d\n" +
            "   • Complaints Resolved Today: %d\n" +
            "   • Pending Escalations: %d\n" +
            "   • Top Categories: %s\n\n" +
            "⚠️ Action Required:\n" +
            "1. Review pending escalations\n" +
            "2. Check high priority complaints\n" +
            "3. Follow up on overdue complaints\n\n" +
            "Access the dashboard at: http://localhost:3000/admin/dashboard\n\n" +
            "Best regards,\n" +
            "IT Grievance System",
            newComplaints, resolvedToday, pendingEscalations, topCategories
        );
        
        sendEscalationEmail(adminEmail, subject, body);
    }
    
    @Async
    public void sendWelcomeEmail(String userEmail, String userName) {
        String subject = "👋 Welcome to IT Grievance System!";
        
        String body = String.format(
            "Dear %s,\n\n" +
            "Welcome to the IT Grievance System!\n\n" +
            "Your account has been successfully created. You can now:\n" +
            "1. Submit new complaints\n" +
            "2. Track your complaint status\n" +
            "3. Communicate with support staff\n" +
            "4. Upload supporting documents\n\n" +
            "Login to your account at: http://localhost:3000/login\n\n" +
            "If you have any questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "IT Grievance System Team\n" +
            "----------------------------\n" +
            "This is an automated welcome email.",
            userName
        );
        
        sendEscalationEmail(userEmail, subject, body);
    }
    
    @Async
    public void sendVerificationEmail(String userEmail, String userName, String verificationCode) {
        String subject = "🔐 Verify Your IT Grievance System Account";
        
        String body = String.format(
            "Dear %s,\n\n" +
            "Thank you for registering with IT Grievance System.\n\n" +
            "Your verification code is: %s\n\n" +
            "Please enter this code on the verification page to activate your account.\n\n" +
            "This code will expire in 24 hours.\n\n" +
            "If you didn't create an account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "IT Grievance System Team\n" +
            "----------------------------\n" +
            "This is an automated verification email.",
            userName, verificationCode
        );
        
        sendEscalationEmail(userEmail, subject, body);
    }
    
    @Async
    public void sendPasswordResetEmail(String userEmail, String resetToken) {
        String subject = "🔑 Reset Your IT Grievance System Password";
        
        String body = String.format(
            "Dear User,\n\n" +
            "We received a request to reset your password.\n\n" +
            "Reset Token: %s\n\n" +
            "Use this token to reset your password on the reset password page.\n\n" +
            "If you didn't request a password reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "IT Grievance System Security Team\n" +
            "----------------------------\n" +
            "This is an automated password reset email.",
            resetToken
        );
        
        sendEscalationEmail(userEmail, subject, body);
    }
    
    private String getTimeLimitByPriority(String priority) {
        if (priority == null || priority.equalsIgnoreCase("MEDIUM")) {
            return "24 hours";
        } else if (priority.equalsIgnoreCase("HIGH")) {
            return "12 hours";
        } else if (priority.equalsIgnoreCase("LOW")) {
            return "48 hours";
        }
        return "24 hours";
    }
}