package com.grievance.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "complaints")
public class Complaint {
    
    public enum Status {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED,
        REJECTED,
        NEW,         // ADDED for analytics
        UNDER_REVIEW // ADDED for analytics
    }
    
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "assigned_to")
    private String assignedTo;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();
    
    // Add these fields for file upload
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    // Escalation fields
    @Column(name = "escalation_level")
    private Integer escalationLevel = 0;
    
    @Column(name = "escalated_at")
    private Date escalatedAt;
    
    @Column(name = "escalation_recipients")
    private String escalationRecipients;
    
    @Column(name = "next_escalation_time")
    private Date nextEscalationTime;
    
    @Column(name = "escalation_notes", columnDefinition = "TEXT")
    private String escalationNotes;
    
    // NEW: Comments relationship
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    // NEW: Resolution timestamp for analytics
    @Column(name = "resolved_at")
    private Date resolvedAt;
    
    // NEW: Department field for analytics
    @Column(name = "department")
    private String department;
    
    // Getters and Setters for new fields
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public Date getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    // NEW: Helper method for analytics
    public boolean isEscalated() {
        return escalationLevel != null && escalationLevel > 0;
    }
    
    // Existing getters and setters...
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
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
}