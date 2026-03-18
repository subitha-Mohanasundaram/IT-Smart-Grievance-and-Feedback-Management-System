package com.grievance.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "escalation_history")
public class EscalationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;
    
    @Column(name = "escalation_level", nullable = false)
    private Integer escalationLevel;
    
    @Column(name = "escalated_from")
    private String escalatedFrom;
    
    @Column(name = "escalated_to")
    private String escalatedTo;
    
    private String reason;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "escalated_at", nullable = false)
    private Date escalatedAt;
    
    private String recipients;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
    
    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }
    
    public String getEscalatedFrom() { return escalatedFrom; }
    public void setEscalatedFrom(String escalatedFrom) { this.escalatedFrom = escalatedFrom; }
    
    public String getEscalatedTo() { return escalatedTo; }
    public void setEscalatedTo(String escalatedTo) { this.escalatedTo = escalatedTo; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Date getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(Date escalatedAt) { this.escalatedAt = escalatedAt; }
    
    public String getRecipients() { return recipients; }
    public void setRecipients(String recipients) { this.recipients = recipients; }
}