package com.grievance.repository;

import com.grievance.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    List<Complaint> findByUserId(Long userId);
    
    List<Complaint> findByAssignedTo(String assignedTo);
    
    List<Complaint> findByStatus(com.grievance.model.Complaint.Status status);
    
    List<Complaint> findByPriority(com.grievance.model.Complaint.Priority priority);
    
    List<Complaint> findByCategory(String category);
    
    @Query("SELECT c FROM Complaint c WHERE c.status <> 'RESOLVED' AND c.status <> 'CLOSED' AND c.nextEscalationTime <= CURRENT_TIMESTAMP")
    List<Complaint> findComplaintsReadyForEscalation();
    
    @Query("SELECT c FROM Complaint c WHERE c.escalatedAt IS NOT NULL")
    List<Complaint> findEscalatedComplaints();
    
    @Query("SELECT c FROM Complaint c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<Complaint> findByDateRange(@Param("startDate") Date startDate, 
                                     @Param("endDate") Date endDate);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    long countByStatus(@Param("status") com.grievance.model.Complaint.Status status);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.priority = :priority")
    long countByPriority(@Param("priority") com.grievance.model.Complaint.Priority priority);
    
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.category = :category")
    long countByCategory(@Param("category") String category);
    
    @Query("SELECT c FROM Complaint c WHERE c.status IN ('OPEN', 'IN_PROGRESS', 'NEW', 'UNDER_REVIEW') AND (c.escalationLevel IS NULL OR c.escalationLevel = 0) AND c.nextEscalationTime <= CURRENT_TIMESTAMP")
    List<Complaint> findComplaintsForEscalation();
    
    @Query("SELECT c FROM Complaint c WHERE c.escalationLevel > 0 ORDER BY c.escalatedAt DESC")
    List<Complaint> findAllEscalatedComplaints();
    
    @Query("SELECT c FROM Complaint c WHERE c.status = :status AND c.escalationLevel > 0")
    List<Complaint> findEscalatedComplaintsByStatus(@Param("status") com.grievance.model.Complaint.Status status);
}