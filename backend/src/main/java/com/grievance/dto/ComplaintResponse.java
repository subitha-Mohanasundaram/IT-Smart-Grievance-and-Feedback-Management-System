package com.grievance.dto;

import com.grievance.model.Complaint;
import java.util.Date;
import java.util.List;

public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String priority;
    private Long userId;
    private String userName;
    private Date createdAt;
    private Date updatedAt;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileDownloadUrl;
    private Integer escalationLevel;
    private String escalationNotes;
    private Date escalatedAt;
    
    // NEW: Comments field
    private List<CommentResponse> comments;

    public ComplaintResponse(Complaint complaint) {
        this.id = complaint.getId();
        this.title = complaint.getTitle();
        this.description = complaint.getDescription();
        this.category = complaint.getCategory();
        this.status = complaint.getStatus().name();
        this.priority = complaint.getPriority().name();
        this.userId = complaint.getUser().getId();
        this.userName = complaint.getUser().getUsername();
        this.createdAt = complaint.getCreatedAt();
        this.updatedAt = complaint.getUpdatedAt();
        this.fileName = complaint.getFileName();
        this.fileType = complaint.getFileType();
        this.fileSize = complaint.getFileSize();
        this.escalationLevel = complaint.getEscalationLevel();
        this.escalationNotes = complaint.getEscalationNotes();
        this.escalatedAt = complaint.getEscalatedAt();
        if (complaint.getFilePath() != null) {
            this.fileDownloadUrl = "/api/complaints/" + complaint.getId() + "/download";
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public Long getFileSize() { return fileSize; }
    public String getFileDownloadUrl() { return fileDownloadUrl; }
    public Integer getEscalationLevel() { return escalationLevel; }
    public String getEscalationNotes() { return escalationNotes; }
    public Date getEscalatedAt() { return escalatedAt; }
    
    // NEW: Comments getter and setter
    public List<CommentResponse> getComments() { return comments; }
    public void setComments(List<CommentResponse> comments) { this.comments = comments; }
}