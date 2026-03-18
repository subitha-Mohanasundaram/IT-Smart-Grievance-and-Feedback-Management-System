package com.grievance.dto;

public class CommentRequest {
    private Long complaintId;
    private String content;
    private String type;
    private Boolean isAdminOnly;
    private String attachmentPath;
    
    // Getters and Setters
    public Long getComplaintId() {
        return complaintId;
    }
    
    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
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
}