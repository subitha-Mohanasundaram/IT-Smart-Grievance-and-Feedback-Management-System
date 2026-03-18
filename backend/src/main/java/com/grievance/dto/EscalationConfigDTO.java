package com.grievance.dto;

public class EscalationConfigDTO {
    private Long id;
    private Integer level;
    private Integer timeLimitHours;
    private String assigneeRole;
    private String recipients;
    private Boolean active;
    
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