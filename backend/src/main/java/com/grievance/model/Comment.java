package com.grievance.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "type")  // REMOVE @Enumerated annotation
    private String type = "PUBLIC";  // Changed from CommentType to String
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;
    
    @Column(name = "is_admin_only")
    private Boolean isAdminOnly = false;
    
    private String attachmentPath;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and Setters
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
        if (type == null || type.isEmpty()) {
            this.type = "PUBLIC";
        } else {
            this.type = type.toUpperCase();
        }
    }
    
    public User getUser() { 
        return user; 
    }
    
    public void setUser(User user) { 
        this.user = user; 
    }
    
    public Complaint getComplaint() { 
        return complaint; 
    }
    
    public void setComplaint(Complaint complaint) { 
        this.complaint = complaint; 
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
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + (content != null ? (content.length() > 20 ? content.substring(0, 20) + "..." : content) : "null") + '\'' +
                ", type='" + type + '\'' +
                ", isAdminOnly=" + isAdminOnly +
                ", createdAt=" + createdAt +
                '}';
    }
}