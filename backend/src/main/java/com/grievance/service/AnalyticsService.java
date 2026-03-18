package com.grievance.service;

import com.grievance.dto.AnalyticsDTO;
import com.grievance.model.Complaint;
import com.grievance.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final ComplaintRepository complaintRepository;
    
    public AnalyticsDTO getDashboardAnalytics() {
        System.out.println("📊 Generating dashboard analytics...");
        
        AnalyticsDTO analytics = new AnalyticsDTO();
        
        try {
            List<Complaint> allComplaints = complaintRepository.findAll();
            
            // Set basic counts
            analytics.setTotalComplaints(allComplaints.size());
            analytics.setResolvedComplaints(allComplaints.stream()
                .filter(c -> "RESOLVED".equals(c.getStatus().name()))
                .count());
            analytics.setPendingComplaints(allComplaints.stream()
                .filter(c -> !"RESOLVED".equals(c.getStatus().name()))
                .count());
            analytics.setEscalatedComplaints(allComplaints.stream()
                .filter(c -> c.getEscalationLevel() != null && c.getEscalationLevel() > 0)
                .count());
            
            // Calculate resolution rate
            if (analytics.getTotalComplaints() > 0) {
                double resolutionRate = ((double) analytics.getResolvedComplaints() / analytics.getTotalComplaints()) * 100;
                analytics.setResolutionRate(Math.round(resolutionRate * 100.0) / 100.0);
            } else {
                analytics.setResolutionRate(0.0);
            }
            
            // Set average resolution time (placeholder)
            analytics.setAverageResolutionTime(24.5);
            
            // Generate category distribution
            Map<String, Long> categoryDist = allComplaints.stream()
                .collect(Collectors.groupingBy(
                    Complaint::getCategory,
                    Collectors.counting()
                ));
            analytics.setCategoryDistribution(categoryDist);
            
            // Generate status distribution
            Map<String, Long> statusDist = allComplaints.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getStatus().name(),
                    Collectors.counting()
                ));
            analytics.setStatusDistribution(statusDist);
            
            // Generate priority distribution
            Map<String, Long> priorityDist = allComplaints.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getPriority().name(),
                    Collectors.counting()
                ));
            analytics.setPriorityDistribution(priorityDist);
            
            // Generate daily trend (last 7 days)
            List<AnalyticsDTO.DailyStat> dailyTrend = generateDailyTrend();
            analytics.setDailyTrend(dailyTrend);
            
            // Generate department stats
            List<AnalyticsDTO.DepartmentStat> deptStats = generateDepartmentStats();
            analytics.setDepartmentStats(deptStats);
            
            System.out.println("✅ Analytics generated successfully");
            System.out.println("   Total complaints: " + analytics.getTotalComplaints());
            System.out.println("   Resolved: " + analytics.getResolvedComplaints());
            System.out.println("   Resolution rate: " + analytics.getResolutionRate() + "%");
            
        } catch (Exception e) {
            System.out.println("❌ Error generating analytics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return analytics;
    }
    
    private List<AnalyticsDTO.DailyStat> generateDailyTrend() {
        List<AnalyticsDTO.DailyStat> dailyStats = new ArrayList<>();
        
        // Generate last 7 days
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateStr = date.toLocalDate().toString();
            
            AnalyticsDTO.DailyStat stat = new AnalyticsDTO.DailyStat();
            stat.setDate(dateStr);
            stat.setComplaints(5 + i); // Sample data
            stat.setResolved(3 + i);   // Sample data
            
            dailyStats.add(stat);
        }
        
        return dailyStats;
    }
    
    private List<AnalyticsDTO.DepartmentStat> generateDepartmentStats() {
        List<AnalyticsDTO.DepartmentStat> deptStats = new ArrayList<>();
        
        // Sample departments
        String[] departments = {"IT", "HR", "Finance", "Operations", "Support"};
        
        for (String dept : departments) {
            AnalyticsDTO.DepartmentStat stat = new AnalyticsDTO.DepartmentStat();
            stat.setDepartment(dept);
            stat.setTotalAssigned(10 + new Random().nextInt(20)); // Sample data
            stat.setResolved(5 + new Random().nextInt(10));       // Sample data
            
            if (stat.getTotalAssigned() > 0) {
                double rate = ((double) stat.getResolved() / stat.getTotalAssigned()) * 100;
                stat.setResolutionRate(Math.round(rate * 100.0) / 100.0);
            } else {
                stat.setResolutionRate(0.0);
            }
            
            deptStats.add(stat);
        }
        
        // Sort by total assigned (descending)
        deptStats.sort((a, b) -> Long.compare(b.getTotalAssigned(), a.getTotalAssigned()));
        
        return deptStats;
    }
}