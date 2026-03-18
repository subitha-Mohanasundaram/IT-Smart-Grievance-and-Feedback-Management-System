package com.grievance.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String content;
    private String type;  // Changed from CommentType to String
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
    private Boolean isAdminOnly;
    private String attachmentPath;
    
    // Author info
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String authorEmail;
    
    // Complaint info
    private Long complaintId;
    private String complaintTitle;
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getFormattedCreatedAt() {
        return formattedCreatedAt;
    }
    
    public void setFormattedCreatedAt(String formattedCreatedAt) {
        this.formattedCreatedAt = formattedCreatedAt;
    }
    
    public Boolean getIsAdminOnly() {
        return isAdminOnly;
    }
    
    public void setIsAdminOnly(Boolean isAdminOnly) {
        this.isAdminOnly = isAdminOnly;
    }
    
    public String getAttachmentPath() {
        return attachmentPath;
    }
    
    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorRole() {
        return authorRole;
    }
    
    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }
    
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    public Long getComplaintId() {
        return complaintId;
    }
    
    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }
    
    public String getComplaintTitle() {
        return complaintTitle;
    }
    
    public void setComplaintTitle(String complaintTitle) {
        this.complaintTitle = complaintTitle;
    }
    
    @Override
    public String toString() {
        return "CommentResponse{" +
                "id=" + id +
                ", content='" + (content != null ? (content.length() > 20 ? content.substring(0, 20) + "..." : content) : "null") + '\'' +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", isAdminOnly=" + isAdminOnly +
                ", authorName='" + authorName + '\'' +
                '}';
    }
}