package com.grievance.repository;

import com.grievance.model.EscalationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EscalationHistoryRepository extends JpaRepository<EscalationHistory, Long> {
    List<EscalationHistory> findByComplaintIdOrderByEscalatedAtDesc(Long complaintId);
}