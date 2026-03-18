package com.grievance.model;

import javax.persistence.*;

@Entity
@Table(name = "escalation_config")
public class EscalationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer level;
    
    @Column(name = "time_limit_hours", nullable = false)
    private Integer timeLimitHours;
    
    @Column(name = "assignee_role", nullable = false)
    private String assigneeRole;
    
    @Column(nullable = false)
    private String recipients; // comma-separated emails
    
    private Boolean active = true;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public Integer getTimeLimitHours() { return timeLimitHours; }
    public void setTimeLimitHours(Integer timeLimitHours) { this.timeLimitHours = timeLimitHours; }
    
    public String getAssigneeRole() { return assigneeRole; }
    public void setAssigneeRole(String assigneeRole) { this.assigneeRole = assigneeRole; }
    
    public String getRecipients() { return recipients; }
    public void setRecipients(String recipients) { this.recipients = recipients; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}