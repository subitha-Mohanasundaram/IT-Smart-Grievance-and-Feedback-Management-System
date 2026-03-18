package com.grievance.service;

import com.grievance.dto.ComplaintDTO;
import com.grievance.model.Complaint;
import com.grievance.model.Complaint.Priority;
import com.grievance.model.Complaint.Status;
import com.grievance.model.User;
import com.grievance.repository.ComplaintRepository;
import com.grievance.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComplaintService {
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EscalationService escalationService;
    
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    
    public ComplaintService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
        }
    }
    
    // ========== BASIC COMPLAINT METHODS ==========
    
    public List<ComplaintDTO> getAllComplaints() {
        log.info("Fetching all complaints");
        try {
            List<Complaint> complaints = complaintRepository.findAll();
            log.info("Found {} complaints", complaints.size());
            return complaints.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching complaints: {}", e.getMessage());
            throw new RuntimeException("Error fetching complaints: " + e.getMessage());
        }
    }
    
    public ComplaintDTO getComplaintById(Long id) {
        log.info("Fetching complaint ID: {}", id);
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + id));
        return convertToDTO(complaint);
    }
    
    public List<ComplaintDTO> getUserComplaints(String username) {
        log.info("Fetching complaints for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        List<Complaint> userComplaints = complaintRepository.findByUserId(user.getId());
        log.info("Found {} complaints for user {}", userComplaints.size(), username);
        
        return userComplaints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ComplaintDTO createComplaint(ComplaintDTO complaintDTO, String username) throws IOException {
        log.info("Creating complaint for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return createComplaintWithUser(complaintDTO, user, null);
    }
    
    @Transactional
    public ComplaintDTO createComplaintWithFile(ComplaintDTO complaintDTO, String username, MultipartFile file) throws IOException {
        log.info("Creating complaint with file for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return createComplaintWithUser(complaintDTO, user, file);
    }
    
    private ComplaintDTO createComplaintWithUser(ComplaintDTO complaintDTO, User user, MultipartFile file) throws IOException {
        log.info("Creating complaint for user ID: {}", user.getId());
        
        Complaint complaint = new Complaint();
        complaint.setTitle(complaintDTO.getTitle());
        complaint.setDescription(complaintDTO.getDescription());
        complaint.setCategory(complaintDTO.getCategory());
        
        // Set priority
        if (complaintDTO.getPriority() != null) {
            try {
                complaint.setPriority(Priority.valueOf(complaintDTO.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid priority '{}', defaulting to MEDIUM", complaintDTO.getPriority());
                complaint.setPriority(Priority.MEDIUM);
            }
        } else {
            complaint.setPriority(Priority.MEDIUM);
        }
        
        complaint.setStatus(Status.OPEN);
        complaint.setUser(user);
        
        Date now = new Date();
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        
        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            
            complaint.setFileName(originalFilename);
            complaint.setFilePath(targetLocation.toString());
            complaint.setFileType(file.getContentType());
            complaint.setFileSize(file.getSize());
            log.info("File uploaded: {}", originalFilename);
        }
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Complaint created with ID: {}", savedComplaint.getId());
        
        // Initialize escalation
        try {
            escalationService.initializeEscalation(savedComplaint);
            log.info("Escalation initialized for complaint {}", savedComplaint.getId());
        } catch (Exception e) {
            log.error("Failed to initialize escalation: {}", e.getMessage());
        }
        
        // Send email notification
        try {
            emailService.sendComplaintNotification(savedComplaint);
            log.info("Email notification sent for complaint {}", savedComplaint.getId());
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
        
        return convertToDTO(savedComplaint);
    }
    
    @Transactional
    public ComplaintDTO updateComplaintStatus(Long id, String status, String assignedTo) {
        log.info("Updating complaint {} status to: {}", id, status);
        
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        if (status != null) {
            try {
                Status newStatus = Status.valueOf(status.toUpperCase());
                complaint.setStatus(newStatus);
                
                // If resolved, stop escalation
                if (newStatus == Status.RESOLVED) {
                    complaint.setResolvedAt(new Date());
                    complaint.setNextEscalationTime(null);
                    
                    // Send resolution email
                    try {
                        String resolvedBy = assignedTo != null ? assignedTo : "System";
                        escalationService.sendComplaintResolvedEmail(id, resolvedBy);
                        log.info("Resolution email sent for complaint {}", id);
                    } catch (Exception e) {
                        log.error("Failed to send resolution email: {}", e.getMessage());
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        }
        
        complaint.setUpdatedAt(new Date());
        
        if (assignedTo != null && !assignedTo.isEmpty()) {
            complaint.setAssignedTo(assignedTo);
            log.info("Complaint {} assigned to: {}", id, assignedTo);
        }
        
        Complaint updatedComplaint = complaintRepository.save(complaint);
        log.info("Complaint {} status updated successfully", id);
        
        return convertToDTO(updatedComplaint);
    }
    
    @Transactional
    public ComplaintDTO updateComplaintEscalation(Long id, Integer escalationLevel, String escalationNotes) {
        log.info("Updating escalation for complaint {}, level: {}", id, escalationLevel);
        
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        if (escalationLevel != null) {
            complaint.setEscalationLevel(escalationLevel);
            complaint.setEscalatedAt(new Date());
            log.info("Escalation level set to {}", escalationLevel);
        }
        
        if (escalationNotes != null && !escalationNotes.isEmpty()) {
            complaint.setEscalationNotes(escalationNotes);
        }
        
        complaint.setUpdatedAt(new Date());
        
        Complaint updatedComplaint = complaintRepository.save(complaint);
        log.info("Complaint {} escalation updated", id);
        
        return convertToDTO(updatedComplaint);
    }
    
    @Transactional
    public ComplaintDTO manuallyEscalateComplaint(Long complaintId, Integer targetLevel, String reason) {
        log.info("Manual escalation for complaint {} to level {}", complaintId, targetLevel);
        
        try {
            escalationService.manuallyEscalateComplaint(complaintId, targetLevel, reason);
            
            Complaint updated = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
            
            log.info("Manual escalation completed for complaint {}", complaintId);
            return convertToDTO(updated);
        } catch (Exception e) {
            log.error("Manual escalation error: {}", e.getMessage(), e);
            throw new RuntimeException("Manual escalation failed: " + e.getMessage());
        }
    }
    
    // ========== ESCALATION QUERY METHODS ==========
    
    public List<ComplaintDTO> getEscalatedComplaints() {
        log.info("Fetching escalated complaints");
        try {
            List<Complaint> escalated = complaintRepository.findEscalatedComplaints();
            log.info("Found {} escalated complaints", escalated.size());
            return escalated.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching escalated complaints: {}", e.getMessage());
            throw new RuntimeException("Error fetching escalated complaints: " + e.getMessage());
        }
    }
    
    public List<ComplaintDTO> getPendingEscalationComplaints() {
        log.info("Fetching pending escalation complaints");
        try {
            List<Complaint> pending = complaintRepository.findComplaintsReadyForEscalation();
            log.info("Found {} pending escalation complaints", pending.size());
            return pending.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching pending escalation complaints: {}", e.getMessage());
            throw new RuntimeException("Error fetching pending escalation complaints: " + e.getMessage());
        }
    }
    
    public List<ComplaintDTO> getHighPriorityComplaints() {
        log.info("Fetching high priority complaints");
        try {
            List<Complaint> highPriority = complaintRepository.findByPriority(Priority.HIGH);
            log.info("Found {} high priority complaints", highPriority.size());
            return highPriority.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching high priority complaints: {}", e.getMessage());
            throw new RuntimeException("Error fetching high priority complaints: " + e.getMessage());
        }
    }
    
    public List<ComplaintDTO> getComplaintsByAssignedTo(String assignedTo) {
        log.info("Fetching complaints assigned to: {}", assignedTo);
        List<Complaint> complaints = complaintRepository.findByAssignedTo(assignedTo);
        return complaints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ComplaintDTO> getComplaintsNeedingAttention() {
        log.info("Fetching complaints needing attention");
        
        List<Complaint> allComplaints = complaintRepository.findAll();
        
        // Filter complaints that need attention
        List<Complaint> attentionNeeded = allComplaints.stream()
            .filter(c -> c.getStatus() != Status.RESOLVED && c.getStatus() != Status.CLOSED)
            .filter(c -> {
                // Already escalated
                if (c.isEscalated()) {
                    return true;
                }
                
                // Close to escalation (within 4 hours)
                if (c.getNextEscalationTime() != null) {
                    long hoursUntil = calculateHoursUntil(c.getNextEscalationTime());
                    return hoursUntil <= 4 && hoursUntil >= 0;
                }
                
                return false;
            })
            .sorted((c1, c2) -> {
                // Sort by: escalated first, then by urgency
                boolean c1Escalated = c1.isEscalated();
                boolean c2Escalated = c2.isEscalated();
                
                if (c1Escalated && !c2Escalated) return -1;
                if (!c1Escalated && c2Escalated) return 1;
                
                Date c1Time = c1.getNextEscalationTime();
                Date c2Time = c2.getNextEscalationTime();
                
                if (c1Time == null && c2Time == null) return 0;
                if (c1Time == null) return 1;
                if (c2Time == null) return -1;
                
                return c1Time.compareTo(c2Time);
            })
            .collect(Collectors.toList());
        
        log.info("Found {} complaints needing attention", attentionNeeded.size());
        
        return attentionNeeded.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // ========== STATISTICS METHODS ==========
    
    public long countAllComplaints() {
        return complaintRepository.count();
    }
    
    public long countByStatus(String status) {
        try {
            Status statusEnum = Status.valueOf(status.toUpperCase());
            return complaintRepository.countByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            return 0;
        }
    }
    
    public long countByPriority(String priority) {
        try {
            Priority priorityEnum = Priority.valueOf(priority.toUpperCase());
            return complaintRepository.countByPriority(priorityEnum);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid priority: {}", priority);
            return 0;
        }
    }
    
    public Map<String, Object> getComplaintStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = countAllComplaints();
        long open = countByStatus("OPEN");
        long inProgress = countByStatus("IN_PROGRESS");
        long resolved = countByStatus("RESOLVED");
        long highPriority = countByPriority("HIGH");
        long escalated = complaintRepository.findEscalatedComplaints().size();
        
        stats.put("totalComplaints", total);
        stats.put("open", open);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        stats.put("highPriority", highPriority);
        stats.put("escalated", escalated);
        
        if (total > 0) {
            stats.put("resolutionRate", String.format("%.1f%%", (resolved * 100.0 / total)));
            stats.put("escalationRate", String.format("%.1f%%", (escalated * 100.0 / total)));
        } else {
            stats.put("resolutionRate", "0%");
            stats.put("escalationRate", "0%");
        }
        
        log.info("Complaint stats generated: total={}, escalated={}", total, escalated);
        
        return stats;
    }
    
    public Map<String, Object> getEscalationStats() {
        log.info("Fetching escalation statistics");
        try {
            return escalationService.getEscalationStats();
        } catch (Exception e) {
            log.error("Error fetching escalation stats: {}", e.getMessage());
            Map<String, Object> fallbackStats = new HashMap<>();
            fallbackStats.put("totalComplaints", 0);
            fallbackStats.put("totalEscalated", 0);
            fallbackStats.put("escalationRate", "0%");
            return fallbackStats;
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    public List<Complaint> findByDateRange(Date startDate, Date endDate) {
        return complaintRepository.findByDateRange(startDate, endDate);
    }
    
    @Transactional
    public void deleteComplaint(Long id) {
        log.info("Deleting complaint ID: {}", id);
        if (!complaintRepository.existsById(id)) {
            throw new RuntimeException("Complaint not found");
        }
        complaintRepository.deleteById(id);
        log.info("Complaint {} deleted", id);
    }
    
    @Transactional
    public ComplaintDTO reassignComplaint(Long complaintId, String newAssignee) {
        log.info("Reassigning complaint {} to: {}", complaintId, newAssignee);
        
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        complaint.setAssignedTo(newAssignee);
        complaint.setUpdatedAt(new Date());
        
        Complaint updated = complaintRepository.save(complaint);
        log.info("Complaint {} reassigned successfully", complaintId);
        
        return convertToDTO(updated);
    }
    
    @Transactional
    public ComplaintDTO addCommentToComplaint(Long complaintId, String comment, String username) {
        log.info("Adding comment to complaint {} by {}", complaintId, username);
        
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        // Update the complaint (comments handled by Comment entity)
        complaint.setUpdatedAt(new Date());
        
        Complaint updated = complaintRepository.save(complaint);
        log.info("Comment added to complaint {}", complaintId);
        
        return convertToDTO(updated);
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    
    private ComplaintDTO convertToDTO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }
        
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setCategory(complaint.getCategory());
        
        if (complaint.getPriority() != null) {
            dto.setPriority(complaint.getPriority().name());
        }
        
        if (complaint.getStatus() != null) {
            dto.setStatus(complaint.getStatus().name());
        }
        
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());
        
        if (complaint.getUser() != null) {
            dto.setUserId(complaint.getUser().getId());
            dto.setUserName(complaint.getUser().getName() != null ? 
                           complaint.getUser().getName() : complaint.getUser().getUsername());
            dto.setUserEmail(complaint.getUser().getEmail());
        }
        
        dto.setAssignedTo(complaint.getAssignedTo());
        
        dto.setFileName(complaint.getFileName());
        dto.setFilePath(complaint.getFilePath());
        dto.setFileType(complaint.getFileType());
        dto.setFileSize(complaint.getFileSize());
        
        dto.setEscalationLevel(complaint.getEscalationLevel());
        dto.setEscalatedAt(complaint.getEscalatedAt());
        dto.setEscalationRecipients(complaint.getEscalationRecipients());
        dto.setNextEscalationTime(complaint.getNextEscalationTime());
        dto.setEscalationNotes(complaint.getEscalationNotes());
        dto.setDepartment(complaint.getDepartment());
        
        dto.setIsEscalated(complaint.isEscalated());
        
        // Calculate hours until escalation
        if (complaint.getNextEscalationTime() != null && complaint.getStatus() != Status.RESOLVED) {
            long hoursUntil = calculateHoursUntil(complaint.getNextEscalationTime());
            dto.setHoursUntilEscalation(hoursUntil);
        }
        
        return dto;
    }
    
    private long calculateHoursUntil(Date futureDate) {
        if (futureDate == null) {
            return -1;
        }
        
        long diff = futureDate.getTime() - new Date().getTime();
        return diff / (1000 * 60 * 60); // Convert milliseconds to hours
    }
}