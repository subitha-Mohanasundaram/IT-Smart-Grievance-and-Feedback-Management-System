package com.grievance.service;

import com.grievance.dto.CommentRequest;
import com.grievance.dto.CommentResponse;
import com.grievance.model.Comment;
import com.grievance.model.Complaint;
import com.grievance.model.User;
import com.grievance.repository.CommentRepository;
import com.grievance.repository.ComplaintRepository;
import com.grievance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // ========== ADD COMMENT WITH FILE ==========
    @Transactional
    public CommentResponse addComment(CommentRequest request, MultipartFile file) throws IOException {
        System.out.println("\n💬 ========== ADDING COMMENT ==========");
        System.out.println("Complaint ID: " + request.getComplaintId());
        System.out.println("Content: " + (request.getContent() != null && request.getContent().length() > 50 ? 
            request.getContent().substring(0, 50) + "..." : request.getContent()));
        System.out.println("Type: " + request.getType());
        System.out.println("Is Admin Only: " + request.getIsAdminOnly());
        
        try {
            // Validate complaint exists
            Complaint complaint = complaintRepository.findById(request.getComplaintId())
                    .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + request.getComplaintId()));
            System.out.println("✅ Complaint found: " + complaint.getId() + " - " + complaint.getTitle());
            
            // Get current user (temporary - hardcoded as admin)
            String currentUsername = "admin";
            System.out.println("👤 Using username: " + currentUsername);
            
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
            System.out.println("✅ User found: " + user.getId() + " - " + user.getUsername());
            
            // Create and save comment
            Comment comment = new Comment();
            comment.setContent(request.getContent());
            
            // Handle type - ensure it's valid
            String type = request.getType();
            if (type == null || type.isEmpty()) {
                type = "PUBLIC";
            }
            type = type.toUpperCase();
            if (!type.equals("PUBLIC") && !type.equals("INTERNAL")) {
                type = "PUBLIC";
            }
            comment.setType(type);
            
            System.out.println("✅ Comment type set to: " + type);
            
            comment.setIsAdminOnly(request.getIsAdminOnly() != null && request.getIsAdminOnly());
            comment.setCreatedAt(LocalDateTime.now());
            comment.setComplaint(complaint);
            comment.setUser(user);
            
            System.out.println("📝 Comment details:");
            System.out.println("   - Content: " + comment.getContent());
            System.out.println("   - Type: " + comment.getType());
            System.out.println("   - IsAdminOnly: " + comment.getIsAdminOnly());
            System.out.println("   - CreatedAt: " + comment.getCreatedAt());
            
            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                System.out.println("📎 Processing file: " + file.getOriginalFilename());
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path uploadDir = Paths.get("uploads/comments").toAbsolutePath().normalize();
                
                // Create directory if it doesn't exist
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    System.out.println("📁 Created directory: " + uploadDir);
                }
                
                Path targetLocation = uploadDir.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation);
                
                comment.setAttachmentPath(targetLocation.toString());
                System.out.println("✅ File saved to: " + targetLocation);
            } else {
                System.out.println("📎 No file attached");
            }
            
            // Save the comment
            Comment savedComment = commentRepository.save(comment);
            System.out.println("💾 Comment saved successfully! ID: " + savedComment.getId());
            
            // Send notification if email service is available
            try {
                if (emailService != null) {
                    sendNotification(savedComment, complaint, user);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Notification failed: " + e.getMessage());
            }
            
            // Convert to response and return
            CommentResponse response = convertToResponse(savedComment);
            System.out.println("✅ CommentResponse created");
            System.out.println("==========================================\n");
            
            return response;
            
        } catch (Exception e) {
            System.out.println("❌ ERROR in addComment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add comment: " + e.getMessage(), e);
        }
    }
    
    // ========== ADD COMMENT WITH USER ID ==========
    @Transactional
    public CommentResponse addComment(CommentRequest request, Long userId) {
        System.out.println("\n💬 ========== ADDING COMMENT WITH USER ID ==========");
        System.out.println("User ID: " + userId);
        
        try {
            // Validate complaint exists
            Complaint complaint = complaintRepository.findById(request.getComplaintId())
                    .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + request.getComplaintId()));
            
            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            // Create and save comment
            Comment comment = new Comment();
            comment.setContent(request.getContent());
            
            // Handle type
            String type = request.getType();
            if (type == null || type.isEmpty()) {
                type = "PUBLIC";
            }
            type = type.toUpperCase();
            if (!type.equals("PUBLIC") && !type.equals("INTERNAL")) {
                type = "PUBLIC";
            }
            comment.setType(type);
            
            comment.setIsAdminOnly(request.getIsAdminOnly() != null && request.getIsAdminOnly());
            comment.setCreatedAt(LocalDateTime.now());
            comment.setComplaint(complaint);
            comment.setUser(user);
            
            // Handle attachment path if provided
            if (request.getAttachmentPath() != null && !request.getAttachmentPath().isEmpty()) {
                comment.setAttachmentPath(request.getAttachmentPath());
            }
            
            Comment savedComment = commentRepository.save(comment);
            System.out.println("✅ Comment saved with ID: " + savedComment.getId());
            
            // Send notification
            try {
                if (emailService != null) {
                    sendNotification(savedComment, complaint, user);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Notification failed: " + e.getMessage());
            }
            
            return convertToResponse(savedComment);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR in addComment with user ID: " + e.getMessage());
            throw new RuntimeException("Failed to add comment: " + e.getMessage(), e);
        }
    }
    
    // ========== GET COMMENTS ==========
    public List<CommentResponse> getCommentsByComplaintId(Long complaintId, boolean adminView) {
        System.out.println("📋 Getting comments for complaint: " + complaintId);
        System.out.println("Admin view: " + adminView);
        
        List<Comment> comments;
        
        if (adminView) {
            // Admin view: show all comments
            comments = commentRepository.findByComplaintId(complaintId);
            System.out.println("👨‍💼 Showing all comments (admin view)");
        } else {
            // User view: only show non-admin-only comments
            comments = commentRepository.findByComplaintIdAndIsAdminOnlyFalse(complaintId);
            System.out.println("👤 Showing only public comments (user view)");
        }
        
        // Sort by creation date descending (newest first)
        comments.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));
        
        System.out.println("✅ Found " + comments.size() + " comments");
        
        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // ========== DELETE COMMENT ==========
    @Transactional
    public boolean deleteComment(Long commentId, String username) {
        System.out.println("🗑️ Deleting comment ID: " + commentId);
        
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            System.out.println("❌ Comment not found with ID: " + commentId);
            return false;
        }
        
        Comment comment = commentOpt.get();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check permissions: only admins or comment owner can delete
        boolean isAdmin = "ADMIN".equals(user.getRole().name());
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        
        if (!isAdmin && !isOwner) {
            System.out.println("❌ Permission denied - not admin or owner");
            throw new RuntimeException("You don't have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
        System.out.println("✅ Comment deleted successfully");
        return true;
    }
    
    // ========== GET COMMENT BY ID ==========
    public CommentResponse getCommentById(Long commentId) {
        System.out.println("🔍 Getting comment by ID: " + commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        System.out.println("✅ Comment found");
        return convertToResponse(comment);
    }
    
    // ========== PRIVATE HELPER METHODS ==========
    private void sendNotification(Comment comment, Complaint complaint, User commenter) {
        if (emailService == null) {
            return;
        }
        
        try {
            User recipient;
            
            // If commenter is admin, notify the complaint creator
            if ("ADMIN".equals(commenter.getRole().name())) {
                recipient = complaint.getUser();
                System.out.println("📧 Sending notification to complaint creator: " + 
                    (recipient != null ? recipient.getEmail() : "null"));
            } else {
                // If user commented, notify assigned admin (if any)
                if (complaint.getAssignedTo() != null && !complaint.getAssignedTo().isEmpty()) {
                    try {
                        Long assignedToId = Long.parseLong(complaint.getAssignedTo());
                        recipient = userRepository.findById(assignedToId).orElse(null);
                        System.out.println("📧 Sending notification to assigned admin ID: " + assignedToId);
                    } catch (NumberFormatException e) {
                        recipient = null;
                    }
                } else {
                    // No assigned admin, skip notification
                    System.out.println("📧 No assigned admin, skipping notification");
                    return;
                }
            }
            
            if (recipient != null) {
                String subject = "New Comment on Complaint #" + complaint.getId();
                String message = String.format(
                    "A new comment has been added to complaint: %s\n\n" +
                    "Comment: %s\n" +
                    "Posted by: %s (%s)\n" +
                    "Date: %s",
                    complaint.getTitle(),
                    comment.getContent(),
                    commenter.getName(),
                    commenter.getRole().name(),
                    comment.getCreatedAt().format(DATE_FORMATTER)
                );
                
                emailService.sendCommentNotification(recipient.getEmail(), subject, message);
                System.out.println("✅ Notification sent to: " + recipient.getEmail());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to send notification: " + e.getMessage());
        }
    }
    
    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setType(comment.getType());
        response.setCreatedAt(comment.getCreatedAt());
        response.setIsAdminOnly(comment.getIsAdminOnly());
        response.setAttachmentPath(comment.getAttachmentPath());
        
        // Format created at for display
        if (comment.getCreatedAt() != null) {
            response.setFormattedCreatedAt(comment.getCreatedAt().format(DATE_FORMATTER));
        }
        
        // Set author info
        if (comment.getUser() != null) {
            response.setAuthorId(comment.getUser().getId());
            response.setAuthorName(comment.getUser().getName());
            response.setAuthorRole(comment.getUser().getRole().name());
            response.setAuthorEmail(comment.getUser().getEmail());
        }
        
        // Set complaint info
        if (comment.getComplaint() != null) {
            response.setComplaintId(comment.getComplaint().getId());
            response.setComplaintTitle(comment.getComplaint().getTitle());
        }
        
        return response;
    }
}