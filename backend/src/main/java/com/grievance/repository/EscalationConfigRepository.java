package com.grievance.repository;

import com.grievance.model.EscalationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EscalationConfigRepository extends JpaRepository<EscalationConfig, Long> {
    List<EscalationConfig> findByActiveTrueOrderByLevelAsc();
    EscalationConfig findByLevel(Integer level);
}