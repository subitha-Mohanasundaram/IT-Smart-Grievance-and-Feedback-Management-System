package com.grievance.dto;

import org.springframework.web.multipart.MultipartFile;
import com.grievance.model.Complaint.Priority;
import lombok.Data;

@Data
public class ComplaintRequest {
    private String title;
    private String description;
    private String category;
    private Priority priority;
    private MultipartFile file;
    private Long userId; // To identify which user is creating the complaint

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}