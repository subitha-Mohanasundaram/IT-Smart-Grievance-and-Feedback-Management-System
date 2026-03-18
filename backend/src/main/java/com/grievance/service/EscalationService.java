package com.grievance.service;

import com.grievance.model.*;
import com.grievance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EscalationService {
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private EscalationConfigRepository escalationConfigRepository;
    
    @Autowired
    private EscalationHistoryRepository escalationHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void checkAndEscalateComplaints() {
        System.out.println("\n🔄 ========== CHECKING FOR ESCALATIONS ==========");
        
        List<Complaint> complaintsToEscalate = complaintRepository.findComplaintsForEscalation();
        System.out.println("📋 Found " + complaintsToEscalate.size() + " complaints ready for escalation");
        
        for (Complaint complaint : complaintsToEscalate) {
            escalateComplaint(complaint);
        }
        
        if (complaintsToEscalate.isEmpty()) {
            System.out.println("✅ No complaints need escalation at this time");
        }
        
        System.out.println("================================================\n");
    }
    
    private boolean shouldEscalateComplaint(Complaint complaint) {
        // If already at highest level, no further escalation
        if (complaint.getEscalationLevel() != null && complaint.getEscalationLevel() >= 1) {
            return false;
        }
        
        // Check if complaint has next escalation time set
        if (complaint.getNextEscalationTime() == null) {
            return false;
        }
        
        // If current time is after next escalation time
        return new Date().after(complaint.getNextEscalationTime());
    }
    
    @Transactional
    public void escalateComplaint(Complaint complaint) {
        try {
            System.out.println("⚠️ Escalating complaint ID: " + complaint.getId() + " - " + complaint.getTitle());
            
            // Get current escalation level
            Integer currentLevel = complaint.getEscalationLevel() != null ? complaint.getEscalationLevel() : 0;
            Integer nextLevel = currentLevel + 1;
            
            // Get escalation config for next level
            EscalationConfig config = escalationConfigRepository.findByLevel(nextLevel);
            
            if (config == null) {
                System.out.println("❌ No escalation config found for level " + nextLevel);
                return;
            }
            
            System.out.println("📈 Escalating from level " + currentLevel + " to level " + nextLevel);
            System.out.println("🎯 Assignee Role: " + config.getAssigneeRole());
            
            // Get priority
            String priority = complaint.getPriority() != null ? complaint.getPriority().name() : "MEDIUM";
            
            // Update complaint
            complaint.setEscalationLevel(nextLevel);
            complaint.setEscalatedAt(new Date());
            
            if (config.getRecipients() != null) {
                complaint.setEscalationRecipients(config.getRecipients());
            }
            
            if (config.getAssigneeRole() != null) {
                complaint.setAssignedTo(config.getAssigneeRole());
            }
            
            // Set next escalation time based on config
            if (shouldSetNextEscalation(nextLevel)) {
                Date nextEscalation = calculateNextEscalationTime(config, priority);
                complaint.setNextEscalationTime(nextEscalation);
                System.out.println("⏰ Next escalation scheduled for: " + nextEscalation);
            } else {
                complaint.setNextEscalationTime(null);
                System.out.println("🏁 Final escalation level reached");
            }
            
            // Save complaint
            complaintRepository.save(complaint);
            
            // Create escalation history
            createEscalationHistory(complaint, config, currentLevel, nextLevel, priority);
            
            // Send notifications
            sendEscalationNotifications(complaint, config, priority);
            
            System.out.println("✅ Complaint " + complaint.getId() + " escalated to level " + nextLevel);
            
        } catch (Exception e) {
            System.out.println("❌ Error escalating complaint " + complaint.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean shouldSetNextEscalation(Integer currentLevel) {
        // Check if there's a config for the next level
        EscalationConfig nextConfig = escalationConfigRepository.findByLevel(currentLevel + 1);
        return nextConfig != null;
    }
    
    private Date calculateNextEscalationTime(EscalationConfig config, String priority) {
        // Use config time limit or priority-based time limit
        int hoursToAdd = config.getTimeLimitHours() != null ? 
            config.getTimeLimitHours() : getTimeLimitByPriority(priority);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, hoursToAdd);
        return calendar.getTime();
    }
    
    @Transactional
    public void createEscalationHistory(Complaint complaint, EscalationConfig config, 
                                       Integer fromLevel, Integer toLevel, String priority) {
        EscalationHistory history = new EscalationHistory();
        history.setComplaint(complaint);
        history.setEscalationLevel(toLevel);
        history.setEscalatedFrom(getRoleForLevel(fromLevel));
        history.setEscalatedTo(config.getAssigneeRole());
        history.setReason(priority + " priority complaint not resolved within time limit");
        history.setEscalatedAt(new Date());
        
        if (config.getRecipients() != null) {
            history.setRecipients(config.getRecipients());
        }
        
        escalationHistoryRepository.save(history);
    }
    
    private String getRoleForLevel(Integer level) {
        if (level == null || level == 0) {
            return "USER/INITIAL";
        } else if (level == 1) {
            return "ADMIN";
        }
        return "LEVEL_" + level;
    }
    
    private void sendEscalationNotifications(Complaint complaint, EscalationConfig config, String priority) {
        System.out.println("📧 Sending escalation notifications...");
        
        try {
            // Get user info
            String userEmail = null;
            String userName = null;
            
            if (complaint.getUser() != null) {
                userEmail = complaint.getUser().getEmail();
                userName = complaint.getUser().getUsername();
                System.out.println("📧 User: " + userName + " (" + userEmail + ")");
            }
            
            // 1. Send email to USER
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                try {
                    emailService.sendUserEscalationNotification(
                        userEmail.trim(),
                        complaint.getId(),
                        complaint.getTitle(),
                        config.getLevel(),
                        priority
                    );
                    System.out.println("✅ User email sent to: " + userEmail);
                } catch (Exception e) {
                    System.out.println("⚠️ Could not send email to user: " + e.getMessage());
                }
            }
            
            // 2. Send to configured recipients
            if (config.getRecipients() != null && !config.getRecipients().trim().isEmpty()) {
                String[] recipients = config.getRecipients().split(",");
                for (String recipient : recipients) {
                    String trimmedRecipient = recipient.trim();
                    if (trimmedRecipient.isEmpty()) continue;
                    
                    try {
                        if (config.getLevel() == 1) { // Super Admin level
                            emailService.sendSuperAdminEscalationNotification(
                                trimmedRecipient,
                                complaint.getId(),
                                complaint.getTitle(),
                                userName != null ? userName : "Unknown User",
                                config.getLevel(),
                                priority
                            );
                        } else { // Other admin levels
                            emailService.sendAdminEscalationNotification(
                                trimmedRecipient,
                                complaint.getId(),
                                complaint.getTitle(),
                                userName != null ? userName : "Unknown User",
                                config.getLevel(),
                                config.getAssigneeRole()
                            );
                        }
                        System.out.println("✅ Email sent to: " + trimmedRecipient);
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not send email to " + trimmedRecipient + ": " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error in notification system: " + e.getMessage());
        }
    }
    
    @Transactional
    public void initializeEscalation(Complaint complaint) {
        try {
            System.out.println("🔧 Initializing escalation for complaint ID: " + complaint.getId());
            
            // Get priority
            String priority = complaint.getPriority() != null ? complaint.getPriority().name() : "MEDIUM";
            
            // Get first escalation config (Level 1)
            EscalationConfig firstConfig = escalationConfigRepository.findByLevel(1);
            
            if (firstConfig == null) {
                System.out.println("⚠️ No escalation config found for level 1");
                return;
            }
            
            // Get time limit
            int hoursToEscalation = firstConfig.getTimeLimitHours() != null ? 
                firstConfig.getTimeLimitHours() : getTimeLimitByPriority(priority);
            
            // Set initial escalation data (Level 0 = Initial/User)
            complaint.setEscalationLevel(0);
            
            // Set first escalation time
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, hoursToEscalation);
            complaint.setNextEscalationTime(calendar.getTime());
            
            complaintRepository.save(complaint);
            
            System.out.println("✅ Escalation initialized for complaint " + complaint.getId());
            System.out.println("   Priority: " + priority);
            System.out.println("   Will escalate after " + hoursToEscalation + " hours");
            System.out.println("   First escalation at: " + calendar.getTime());
            
        } catch (Exception e) {
            System.out.println("❌ Error initializing escalation: " + e.getMessage());
        }
    }
    
    // Helper method to get time limit based on priority
    private int getTimeLimitByPriority(String priority) {
        if (priority == null) {
            return 24; // Default for MEDIUM
        }
        
        switch (priority.toUpperCase()) {
            case "HIGH":
                return 12; // 12 hours for HIGH priority
            case "MEDIUM":
                return 24; // 24 hours for MEDIUM priority
            case "LOW":
                return 48; // 48 hours for LOW priority
            default:
                return 24; // Default 24 hours
        }
    }
    
    // Get all escalation configs
    public List<EscalationConfig> getAllEscalationConfigs() {
        return escalationConfigRepository.findByActiveTrueOrderByLevelAsc();
    }
    
    // Save escalation config
    public EscalationConfig saveEscalationConfig(EscalationConfig config) {
        return escalationConfigRepository.save(config);
    }
    
    // Delete escalation config
    public void deleteEscalationConfig(Long id) {
        escalationConfigRepository.deleteById(id);
    }
    
    // Get escalation history for a complaint
    public List<EscalationHistory> getEscalationHistory(Long complaintId) {
        return escalationHistoryRepository.findByComplaintIdOrderByEscalatedAtDesc(complaintId);
    }
    
    @Transactional
    public void manuallyEscalateComplaint(Long complaintId, Integer targetLevel, String reason) {
        System.out.println("\n👨‍💼 ========== MANUAL ESCALATION ==========");
        System.out.println("Complaint ID: " + complaintId);
        System.out.println("Target Level: " + targetLevel);
        
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        EscalationConfig config = escalationConfigRepository.findByLevel(targetLevel);
        if (config == null) {
            throw new RuntimeException("No escalation config found for level " + targetLevel);
        }
        
        // Update complaint
        complaint.setEscalationLevel(targetLevel);
        complaint.setEscalatedAt(new Date());
        
        if (config.getRecipients() != null) {
            complaint.setEscalationRecipients(config.getRecipients());
        }
        
        if (config.getAssigneeRole() != null) {
            complaint.setAssignedTo(config.getAssigneeRole());
        }
        
        complaint.setEscalationNotes("Manual escalation: " + reason);
        complaint.setNextEscalationTime(null);
        
        complaintRepository.save(complaint);
        
        // Create history
        EscalationHistory history = new EscalationHistory();
        history.setComplaint(complaint);
        history.setEscalationLevel(targetLevel);
        history.setEscalatedFrom("Manual Trigger");
        history.setEscalatedTo(config.getAssigneeRole());
        history.setReason("Manual escalation: " + reason);
        history.setEscalatedAt(new Date());
        history.setRecipients(config.getRecipients());
        
        escalationHistoryRepository.save(history);
        
        // Send notifications
        String priority = complaint.getPriority() != null ? complaint.getPriority().name() : "MEDIUM";
        sendEscalationNotifications(complaint, config, priority);
        
        System.out.println("✅ Manual escalation completed for complaint " + complaintId);
        System.out.println("==========================================\n");
    }
    
    @Transactional
    public void sendComplaintResolvedEmail(Long complaintId, String resolvedBy) {
        try {
            Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
            
            String userEmail = null;
            if (complaint.getUser() != null) {
                userEmail = complaint.getUser().getEmail();
            }
            
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                emailService.sendComplaintResolvedNotification(
                    userEmail.trim(),
                    complaint.getId(),
                    complaint.getTitle(),
                    resolvedBy
                );
                System.out.println("✅ Resolution email sent to user: " + userEmail);
            } else {
                System.out.println("⚠️ User email not found for complaint ID: " + complaintId);
            }
        } catch (Exception e) {
            System.out.println("❌ Error sending resolution email: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getEscalationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Complaint> allComplaints = complaintRepository.findAll();
        List<Complaint> escalatedComplaints = complaintRepository.findEscalatedComplaints();
        
        long totalComplaints = allComplaints.size();
        long totalEscalated = escalatedComplaints.size();
        
        // Count by priority
        Map<String, Long> priorityCounts = new HashMap<>();
        for (Complaint c : allComplaints) {
            String priority = c.getPriority() != null ? c.getPriority().name() : "MEDIUM";
            priorityCounts.put(priority, priorityCounts.getOrDefault(priority, 0L) + 1);
        }
        
        // Count escalated by priority
        Map<String, Long> escalatedPriorityCounts = new HashMap<>();
        for (Complaint c : escalatedComplaints) {
            String priority = c.getPriority() != null ? c.getPriority().name() : "MEDIUM";
            escalatedPriorityCounts.put(priority, escalatedPriorityCounts.getOrDefault(priority, 0L) + 1);
        }
        
        stats.put("totalComplaints", totalComplaints);
        stats.put("totalEscalated", totalEscalated);
        stats.put("priorityCounts", priorityCounts);
        stats.put("escalatedPriorityCounts", escalatedPriorityCounts);
        stats.put("escalationRate", totalComplaints > 0 ? 
            String.format("%.1f%%", (totalEscalated * 100.0 / totalComplaints)) : "0%");
        
        return stats;
    }
}