package com.grievance.dto;

import java.util.Date;

public class ComplaintDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String priority;
    private Long userId;
    private String userName;
    private String userEmail;
    private String assignedTo; // Changed from Long to String
    private Date createdAt;
    private Date updatedAt;
    private Date resolvedAt;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Integer escalationLevel;
    private Date escalatedAt;
    private String escalationRecipients;
    private Date nextEscalationTime;
    private String escalationNotes;
    private String department;
    private Boolean isEscalated;
    private Long hoursUntilEscalation;
    
    // Getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public Date getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Date resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }
    
    public Date getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(Date escalatedAt) { this.escalatedAt = escalatedAt; }
    
    public String getEscalationRecipients() { return escalationRecipients; }
    public void setEscalationRecipients(String escalationRecipients) { this.escalationRecipients = escalationRecipients; }
    
    public Date getNextEscalationTime() { return nextEscalationTime; }
    public void setNextEscalationTime(Date nextEscalationTime) { this.nextEscalationTime = nextEscalationTime; }
    
    public String getEscalationNotes() { return escalationNotes; }
    public void setEscalationNotes(String escalationNotes) { this.escalationNotes = escalationNotes; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public Boolean getIsEscalated() { return isEscalated; }
    public void setIsEscalated(Boolean isEscalated) { this.isEscalated = isEscalated; }
    
    public Long getHoursUntilEscalation() { return hoursUntilEscalation; }
    public void setHoursUntilEscalation(Long hoursUntilEscalation) { this.hoursUntilEscalation = hoursUntilEscalation; }
}