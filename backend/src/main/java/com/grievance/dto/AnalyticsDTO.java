package com.grievance.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDTO {
    private int totalComplaints;
    private long resolvedComplaints;
    private long pendingComplaints;
    private long escalatedComplaints;
    private double resolutionRate;
    private double averageResolutionTime;
    private Map<String, Long> categoryDistribution;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> priorityDistribution;
    private List<DailyStat> dailyTrend;
    private List<DepartmentStat> departmentStats;
    
    // Inner class for DailyStat
    public static class DailyStat {
        private String date;
        private long complaints;
        private long resolved;
        
        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public long getComplaints() { return complaints; }
        public void setComplaints(long complaints) { this.complaints = complaints; }
        
        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }
    }
    
    // Inner class for DepartmentStat
    public static class DepartmentStat {
        private String department;
        private long totalAssigned;
        private long resolved;
        private double resolutionRate;
        
        // Getters and Setters
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public long getTotalAssigned() { return totalAssigned; }
        public void setTotalAssigned(long totalAssigned) { this.totalAssigned = totalAssigned; }
        
        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }
        
        public double getResolutionRate() { return resolutionRate; }
        public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    }
    
    // Getters and Setters for main class
    public int getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(int totalComplaints) { this.totalComplaints = totalComplaints; }
    
    public long getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(long resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }
    
    public long getPendingComplaints() { return pendingComplaints; }
    public void setPendingComplaints(long pendingComplaints) { this.pendingComplaints = pendingComplaints; }
    
    public long getEscalatedComplaints() { return escalatedComplaints; }
    public void setEscalatedComplaints(long escalatedComplaints) { this.escalatedComplaints = escalatedComplaints; }
    
    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    
    public double getAverageResolutionTime() { return averageResolutionTime; }
    public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
    
    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }
    
    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }
    
    public Map<String, Long> getPriorityDistribution() { return priorityDistribution; }
    public void setPriorityDistribution(Map<String, Long> priorityDistribution) { this.priorityDistribution = priorityDistribution; }
    
    public List<DailyStat> getDailyTrend() { return dailyTrend; }
    public void setDailyTrend(List<DailyStat> dailyTrend) { this.dailyTrend = dailyTrend; }
    
    public List<DepartmentStat> getDepartmentStats() { return departmentStats; }
    public void setDepartmentStats(List<DepartmentStat> departmentStats) { this.departmentStats = departmentStats; }
}