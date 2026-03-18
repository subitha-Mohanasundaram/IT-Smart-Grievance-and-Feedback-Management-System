package com.grievance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grievance.dto.*;
import com.grievance.model.*;
import com.grievance.repository.*;
import com.grievance.security.JwtUtil;
import com.grievance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class TestController {
    
    @Autowired
    private ComplaintService complaintService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private EscalationService escalationService;
    
    @Autowired
    private EscalationHistoryRepository escalationHistoryRepository;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private CommentService commentService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // ================== FIXED REGISTRATION ENDPOINT ==================
    
    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n📝 ========== REGISTRATION ATTEMPT ==========");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Full Name: " + request.getFullName());
            
            // Use the AuthService.registerUser method
            User user = authService.registerUser(request);
            
            String token = jwtUtil.generateToken(user.getUsername());
            
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole().name());
            
            System.out.println("✅ Registration successful for: " + user.getUsername());
            System.out.println("✅ User ID: " + user.getId());
            System.out.println("✅ Role: " + user.getRole());
            System.out.println("✅ Token generated");
            System.out.println("=============================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("\n🔐 ========== LOGIN REQUEST ==========");
        System.out.println("Username: " + loginRequest.getUsername());
        
        try {
            Optional<User> userOpt = authService.validateLogin(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            );
            
            if (userOpt.isEmpty()) {
                System.out.println("❌ Invalid username or password");
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return response;
            }
            
            User user = userOpt.get();
            
            String token = jwtUtil.generateToken(user.getUsername());
            
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole().name());
            
            System.out.println("✅ Login successful for: " + user.getUsername());
            System.out.println("✅ User ID: " + user.getId());
            System.out.println("✅ Role: " + user.getRole());
            System.out.println("✅ Token generated");
            System.out.println("=====================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/auth/me")
    public Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🔍 ========== GET CURRENT USER ==========");
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            System.out.println("✅ Username from token: " + username);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            response.put("success", true);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("name", user.getName());
            
            System.out.println("✅ User found: " + user.getUsername());
            System.out.println("=========================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Get current user error: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // ================== FIXED COMMENT ENDPOINTS ==================
    
    @PostMapping(value = "/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> addComment(
            @RequestParam("complaintId") Long complaintId,
            @RequestParam("content") String content,
            @RequestParam(value = "type", required = false, defaultValue = "PUBLIC") String type,
            @RequestParam(value = "isAdminOnly", required = false, defaultValue = "false") Boolean isAdminOnly,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n💬 ========== ADDING COMMENT ==========");
            System.out.println("Complaint ID: " + complaintId);
            System.out.println("Content: " + (content.length() > 50 ? content.substring(0, 50) + "..." : content));
            System.out.println("Type: " + type);
            System.out.println("Is Admin Only: " + isAdminOnly);
            System.out.println("Has File: " + (file != null ? file.getOriginalFilename() : "No file"));
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("User: " + username);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate complaint exists
            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));
            
            // Validate type
            String validatedType = type.toUpperCase();
            if (!isValidCommentType(validatedType)) {
                validatedType = "PUBLIC";
            }
            
            // Check permissions for internal/admin-only comments
            if ((validatedType.equals("INTERNAL") || Boolean.TRUE.equals(isAdminOnly)) && user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Only admins can post internal or admin-only comments");
                return response;
            }
            
            // Create comment request
            CommentRequest commentRequest = new CommentRequest();
            commentRequest.setContent(content);
            commentRequest.setType(validatedType);
            commentRequest.setComplaintId(complaintId);
            commentRequest.setIsAdminOnly(isAdminOnly);
            
            CommentResponse commentResponse = commentService.addComment(commentRequest, file);
            
            response.put("success", true);
            response.put("message", "Comment added successfully");
            response.put("data", commentResponse);
            
            System.out.println("✅ Comment added successfully!");
            System.out.println("Comment ID: " + commentResponse.getId());
            System.out.println("==========================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR adding comment: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error adding comment: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping(value = "/comments/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> addCommentJson(
            @RequestBody CommentRequest commentRequest,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n💬 ========== ADDING COMMENT (JSON) ==========");
            System.out.println("Complaint ID: " + commentRequest.getComplaintId());
            System.out.println("Content: " + (commentRequest.getContent() != null && commentRequest.getContent().length() > 50 ? 
                commentRequest.getContent().substring(0, 50) + "..." : commentRequest.getContent()));
            System.out.println("Type: " + commentRequest.getType());
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("User: " + username);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate complaint exists
            Complaint complaint = complaintRepository.findById(commentRequest.getComplaintId())
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));
            
            // Validate and normalize type
            String type = commentRequest.getType();
            if (type == null || type.isEmpty()) {
                type = "PUBLIC";
            }
            String validatedType = type.toUpperCase();
            if (!isValidCommentType(validatedType)) {
                validatedType = "PUBLIC";
            }
            commentRequest.setType(validatedType);
            
            // Check permissions
            if ((validatedType.equals("INTERNAL") || Boolean.TRUE.equals(commentRequest.getIsAdminOnly())) 
                    && user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Only admins can post internal or admin-only comments");
                return response;
            }
            
            CommentResponse commentResponse = commentService.addComment(commentRequest, user.getId());
            
            response.put("success", true);
            response.put("message", "Comment added successfully");
            response.put("data", commentResponse);
            
            System.out.println("✅ Comment added via JSON!");
            System.out.println("===============================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR adding comment (JSON): " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/complaints/{complaintId}/comments")
    public Map<String, Object> getComments(
            @PathVariable Long complaintId,
            @RequestParam(defaultValue = "false") boolean adminView,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("💬 Getting comments for complaint: " + complaintId);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Validate complaint exists
            complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));
            
            // Check if user is admin
            boolean isAdmin = user.getRole() == Role.ADMIN;
            
            List<CommentResponse> comments = commentService.getCommentsByComplaintId(complaintId, isAdmin && adminView);
            
            response.put("success", true);
            response.put("data", comments);
            response.put("count", comments.size());
            response.put("isAdmin", isAdmin);
            
            System.out.println("✅ Retrieved " + comments.size() + " comments for complaint: " + complaintId);
            
        } catch (Exception e) {
            System.out.println("❌ Error getting comments: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    // ================== NEW: VIEW COMPLAINT DETAILS ENDPOINT ==================
    
    @GetMapping("/complaints/{id}/view")
    public Map<String, Object> getComplaintView(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🔍 ========== VIEW COMPLAINT DETAILS ==========");
            System.out.println("Complaint ID: " + id);
            
            // Validate token
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("User requesting view: " + username);
            
            // Get user
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get complaint
            ComplaintDTO complaint = complaintService.getComplaintById(id);
            if (complaint == null) {
                throw new RuntimeException("Complaint not found with ID: " + id);
            }
            
            // Check authorization
            boolean isAdmin = user.getRole() == Role.ADMIN;
            if (!isAdmin && !complaint.getUserId().equals(user.getId())) {
                throw new RuntimeException("Not authorized to view this complaint");
            }
            
            // Get comments for this complaint
            List<CommentResponse> comments = commentService.getCommentsByComplaintId(id, isAdmin);
            
            // Get escalation history if available
            List<Map<String, Object>> escalationHistory = new ArrayList<>();
            try {
                List<EscalationHistory> historyList = escalationHistoryRepository.findByComplaintIdOrderByEscalatedAtDesc(id);
                
                for (EscalationHistory history : historyList) {
                    Map<String, Object> historyMap = new HashMap<>();
                    historyMap.put("escalatedAt", history.getEscalatedAt());
                    historyMap.put("escalationLevel", history.getEscalationLevel());
                    historyMap.put("notes", history.getNotes());
                    historyMap.put("recipients", history.getRecipients());
                    escalationHistory.add(historyMap);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Escalation history not available: " + e.getMessage());
            }
            
            // Get user info
            Optional<User> complaintUser = userRepository.findById(complaint.getUserId());
            String complaintUserName = complaintUser.map(User::getUsername).orElse("Unknown");
            String complaintUserEmail = complaintUser.map(User::getEmail).orElse("N/A");
            
            // Prepare response
            response.put("success", true);
            response.put("complaint", complaint);
            response.put("comments", comments);
            response.put("escalationHistory", escalationHistory);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "isAdmin", isAdmin
            ));
            response.put("complaintUser", Map.of(
                "id", complaint.getUserId(),
                "username", complaintUserName,
                "email", complaintUserEmail
            ));
            response.put("canAddComment", isAdmin);
            response.put("canDownloadFile", complaint.getFileName() != null);
            
            System.out.println("✅ Complaint view prepared for user: " + username);
            System.out.println("   Complaint ID: " + id);
            System.out.println("   Title: " + complaint.getTitle());
            System.out.println("   Status: " + complaint.getStatus());
            System.out.println("   Comments count: " + comments.size());
            System.out.println("   Escalation history count: " + escalationHistory.size());
            System.out.println("==============================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Error in view complaint: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    // ================== NEW: TEST DTO ENDPOINT ==================
    
    @GetMapping("/test-dto")
    public Map<String, Object> testDto(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🧪 Testing DTO conversion");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            // Create a simple test DTO
            ComplaintDTO testDto = new ComplaintDTO();
            testDto.setId(1L);
            testDto.setTitle("Test Complaint");
            testDto.setDescription("Testing DTO serialization");
            testDto.setCategory("Test");
            testDto.setPriority("MEDIUM");
            testDto.setStatus("OPEN");
            testDto.setCreatedAt(new Date());
            testDto.setUpdatedAt(new Date());
            testDto.setUserId(1L);
            testDto.setUserName("Test User");
            
            response.put("success", true);
            response.put("testDto", testDto);
            response.put("message", "DTO test successful");
            
            System.out.println("✅ DTO test completed for user: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ DTO test error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    // ================== ENHANCED COMPLAINTS ENDPOINT WITH DEBUGGING ==================
    
    @GetMapping("/complaints")
    public Map<String, Object> getAllComplaints(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n📋 ========== GET ALL COMPLAINTS ==========");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("✅ User requesting complaints: " + username);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                System.out.println("❌ User is not admin. Role: " + user.getRole());
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            System.out.println("✅ User is admin, proceeding to fetch complaints...");
            
            List<ComplaintDTO> complaints = complaintService.getAllComplaints();
            System.out.println("✅ Retrieved " + complaints.size() + " complaints from service");
            
            // Debug: Check first few complaints
            if (!complaints.isEmpty()) {
                System.out.println("\n🔍 Sample complaints (first 3):");
                for (int i = 0; i < Math.min(3, complaints.size()); i++) {
                    ComplaintDTO dto = complaints.get(i);
                    System.out.println("  " + (i + 1) + ". ID: " + dto.getId() + 
                                     ", Title: " + dto.getTitle() + 
                                     ", Status: " + dto.getStatus());
                }
            }
            
            response.put("success", true);
            response.put("data", complaints);
            response.put("count", complaints.size());
            response.put("timestamp", new Date());
            
            System.out.println("✅ Successfully returned " + complaints.size() + " complaints for admin: " + username);
            System.out.println("==========================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR getting all complaints: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("errorDetails", e.toString());
            response.put("timestamp", new Date());
        }
        
        return response;
    }
    
    // ================== SIMPLE COMPLAINTS ENDPOINT (Alternative) ==================
    
    @GetMapping("/complaints/simple")
    public Map<String, Object> getSimpleComplaints(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📋 Getting simple complaints list");
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<Complaint> complaints = complaintRepository.findAll();
            List<Map<String, Object>> simpleList = new ArrayList<>();
            
            for (Complaint c : complaints) {
                Map<String, Object> simple = new HashMap<>();
                simple.put("id", c.getId());
                simple.put("title", c.getTitle());
                simple.put("status", c.getStatus().name());
                simple.put("priority", c.getPriority().name());
                simple.put("category", c.getCategory());
                simple.put("createdAt", c.getCreatedAt());
                simple.put("user", c.getUser() != null ? c.getUser().getUsername() : "Unknown");
                simpleList.add(simple);
            }
            
            response.put("success", true);
            response.put("data", simpleList);
            response.put("count", simpleList.size());
            
            System.out.println("✅ Retrieved " + simpleList.size() + " complaints in simple format");
            
        } catch (Exception e) {
            System.out.println("❌ Error getting simple complaints: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // ================== NEW ANALYTICS ENDPOINTS ==================
    
    @GetMapping("/analytics/dashboard")
    public Map<String, Object> getDashboardAnalytics(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 Generating dashboard analytics...");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            AnalyticsDTO analytics = analyticsService.getDashboardAnalytics();
            
            response.put("success", true);
            response.put("data", analytics);
            response.put("message", "Analytics retrieved successfully");
            
            System.out.println("✅ Analytics sent to admin: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ Error getting analytics: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/analytics/export/csv")
    public ResponseEntity<String> exportAnalyticsToCSV(@RequestHeader("Authorization") String authHeader) {
        
        try {
            System.out.println("📊 Exporting analytics to CSV...");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body("Admin access required");
            }
            
            AnalyticsDTO analytics = analyticsService.getDashboardAnalytics();
            
            StringBuilder csv = new StringBuilder();
            
            // Summary section
            csv.append("SUMMARY\n");
            csv.append("Total Complaints,").append(analytics.getTotalComplaints()).append("\n");
            csv.append("Resolved Complaints,").append(analytics.getResolvedComplaints()).append("\n");
            csv.append("Pending Complaints,").append(analytics.getPendingComplaints()).append("\n");
            csv.append("Escalated Complaints,").append(analytics.getEscalatedComplaints()).append("\n");
            csv.append("Resolution Rate,").append(String.format("%.2f%%", analytics.getResolutionRate())).append("\n");
            csv.append("Average Resolution Time (hours),").append(String.format("%.2f", analytics.getAverageResolutionTime())).append("\n\n");
            
            // Category distribution
            csv.append("CATEGORY DISTRIBUTION\n");
            csv.append("Category,Count\n");
            if (analytics.getCategoryDistribution() != null) {
                analytics.getCategoryDistribution().forEach((category, count) -> 
                    csv.append(category).append(",").append(count).append("\n"));
            }
            csv.append("\n");
            
            // Status distribution
            csv.append("STATUS DISTRIBUTION\n");
            csv.append("Status,Count\n");
            if (analytics.getStatusDistribution() != null) {
                analytics.getStatusDistribution().forEach((status, count) -> 
                    csv.append(status).append(",").append(count).append("\n"));
            }
            csv.append("\n");
            
            // Priority distribution
            csv.append("PRIORITY DISTRIBUTION\n");
            csv.append("Priority,Count\n");
            if (analytics.getPriorityDistribution() != null) {
                analytics.getPriorityDistribution().forEach((priority, count) -> 
                    csv.append(priority).append(",").append(count).append("\n"));
            }
            csv.append("\n");
            
            // Daily trend
            csv.append("DAILY TREND (Last 30 Days)\n");
            csv.append("Date,Complaints,Resolved\n");
            if (analytics.getDailyTrend() != null) {
                analytics.getDailyTrend().forEach(day -> 
                    csv.append(day.getDate()).append(",").append(day.getComplaints()).append(",").append(day.getResolved()).append("\n"));
            }
            csv.append("\n");
            
            // Department stats
            csv.append("DEPARTMENT PERFORMANCE\n");
            csv.append("Department,Total Assigned,Resolved,Resolution Rate\n");
            if (analytics.getDepartmentStats() != null) {
                analytics.getDepartmentStats().forEach(dept -> 
                    csv.append(dept.getDepartment()).append(",")
                       .append(dept.getTotalAssigned()).append(",")
                       .append(dept.getResolved()).append(",")
                       .append(String.format("%.2f%%", dept.getResolutionRate())).append("\n"));
            }
            
            // Generate filename with timestamp
            String filename = "complaints_analytics_" + System.currentTimeMillis() + ".csv";
            
            System.out.println("✅ CSV exported successfully for user: " + username);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv.toString());
            
        } catch (Exception e) {
            System.out.println("❌ Error exporting CSV: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error generating CSV: " + e.getMessage());
        }
    }
    
    // ================== ENHANCED COMPLAINT DETAILS ENDPOINT ==================
    
    @GetMapping("/complaints/{id}/details")
    public Map<String, Object> getComplaintDetails(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 Getting complaint details: " + id);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean isAdmin = user.getRole() == Role.ADMIN;
            ComplaintDTO complaintResponse = complaintService.getComplaintById(id);
            
            response.put("success", true);
            response.put("data", complaintResponse);
            response.put("isAdmin", isAdmin);
            
            System.out.println("✅ Complaint details retrieved for user: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ Error getting complaint details: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    // ================== EXISTING ENDPOINTS ==================
    
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        System.out.println("🔧 Test endpoint called");
        response.put("success", true);
        response.put("message", "IT Grievance System API is working!");
        response.put("timestamp", new Date());
        response.put("version", "1.0.0");
        return response;
    }
    
    @GetMapping("/complaints/my-complaints")
    public Map<String, Object> getMyComplaints(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📋 Getting user complaints");
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            List<ComplaintDTO> myComplaints = complaintService.getUserComplaints(username);
            
            response.put("success", true);
            response.put("data", myComplaints);
            response.put("count", myComplaints.size());
            
            System.out.println("✅ Retrieved " + myComplaints.size() + " complaints for user: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ Error getting user complaints: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping(value = "/complaints", consumes = {"multipart/form-data"})
    public Map<String, Object> createComplaint(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "category", required = false, defaultValue = "Other") String category,
            @RequestParam(value = "priority", required = false, defaultValue = "MEDIUM") String priority,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n📝 ========== CREATING NEW COMPLAINT ==========");
            System.out.println("Title: " + title);
            System.out.println("Category: " + category);
            System.out.println("Priority: " + priority);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("✅ User creating complaint: " + username);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            System.out.println("✅ User found: " + user.getUsername() + ", Role: " + user.getRole());
            
            String validPriority = priority.toUpperCase();
            if (!isValidPriority(validPriority)) {
                validPriority = "MEDIUM";
            }
            
            System.out.println("✅ Valid priority: " + validPriority);
            
            ComplaintDTO complaintDTO = new ComplaintDTO();
            complaintDTO.setTitle(title);
            complaintDTO.setDescription(description);
            complaintDTO.setCategory(category);
            complaintDTO.setPriority(validPriority);
            
            ComplaintDTO savedComplaint;
            if (file != null && !file.isEmpty()) {
                System.out.println("📎 File attached: " + file.getOriginalFilename());
                savedComplaint = complaintService.createComplaintWithFile(complaintDTO, username, file);
            } else {
                savedComplaint = complaintService.createComplaint(complaintDTO, username);
            }
            
            response.put("success", true);
            response.put("message", "Complaint created successfully");
            response.put("complaintId", savedComplaint.getId());
            response.put("data", savedComplaint);
            
            System.out.println("✅ Complaint created successfully with ID: " + savedComplaint.getId());
            System.out.println("================================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR creating complaint: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @PutMapping("/complaints/{id}/status")
    public Map<String, Object> updateComplaintStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("✏️ Updating complaint status, ID: " + id);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            String status = request.get("status");
            if (status == null || status.isEmpty()) {
                throw new RuntimeException("Status is required");
            }
            
            ComplaintDTO updated = complaintService.updateComplaintStatus(
                id, 
                status.toUpperCase(), 
                request.get("assignedTo")
            );
            
            response.put("success", true);
            response.put("message", "Status updated to " + status);
            response.put("data", updated);
            
            System.out.println("✅ Complaint status updated by admin: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ Error updating complaint status: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/complaints/{complaintId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long complaintId, 
                                                @RequestHeader("Authorization") String authHeader) throws IOException {
        
        try {
            System.out.println("\n📥 ========== DOWNLOAD FILE REQUEST ==========");
            System.out.println("Complaint ID: " + complaintId);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("User requesting download: " + username);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
            if (complaintOpt.isEmpty()) {
                System.out.println("❌ Complaint not found with ID: " + complaintId);
                throw new RuntimeException("Complaint not found");
            }
            Complaint complaint = complaintOpt.get();
            
            if (complaint.getFileName() == null || complaint.getFileName().isEmpty()) {
                System.out.println("❌ No file attached to this complaint");
                throw new RuntimeException("No file attached to this complaint");
            }
            
            if (user.getRole() != Role.ADMIN && !complaint.getUser().getId().equals(user.getId())) {
                System.out.println("❌ Permission denied");
                throw new RuntimeException("You don't have permission to download this file");
            }
            
            String filePathStr = complaint.getFilePath();
            Path filePath = Paths.get(filePathStr);
            
            if (!Files.exists(filePath)) {
                String fileNameOnly = Paths.get(filePathStr).getFileName().toString();
                Path uploadPath = Paths.get(uploadDir);
                Path alternativePath = uploadPath.resolve(fileNameOnly);
                
                if (Files.exists(alternativePath)) {
                    filePath = alternativePath;
                } else {
                    throw new RuntimeException("File not found on server");
                }
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            String contentType = complaint.getFileType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }
            
            System.out.println("✅ Sending file: " + complaint.getFileName());
            System.out.println("=========================================\n");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + complaint.getFileName() + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            System.out.println("❌ ERROR in download: " + e.getMessage());
            throw new RuntimeException("File download failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        System.out.println("🏥 Health check requested");
        response.put("status", "UP");
        response.put("service", "IT Grievance System");
        response.put("timestamp", new Date());
        response.put("version", "2.0.0");
        return response;
    }
    
    @PostMapping("/test-create")
    public Map<String, Object> testCreateComplaint() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🧪 ========== TEST CREATE COMPLAINT ==========");
            
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                throw new RuntimeException("No users found in database");
            }
            
            User user = users.get(0);
            System.out.println("🧪 Using user: " + user.getUsername());
            
            ComplaintDTO complaintDTO = new ComplaintDTO();
            complaintDTO.setTitle("Test Complaint from API");
            complaintDTO.setDescription("This is a test complaint created via API");
            complaintDTO.setCategory("Software");
            complaintDTO.setPriority("MEDIUM");
            
            ComplaintDTO saved = complaintService.createComplaint(complaintDTO, user.getUsername());
            
            response.put("success", true);
            response.put("message", "Test complaint created");
            response.put("complaintId", saved.getId());
            response.put("title", saved.getTitle());
            response.put("userId", saved.getUserId());
            
            System.out.println("🧪 TEST: Complaint created with ID: " + saved.getId());
            System.out.println("================================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ TEST ERROR: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/test-jwt")
    public Map<String, Object> testJwt(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🔍 ========== TEST JWT TOKEN ==========");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ No Bearer token provided");
                response.put("success", false);
                response.put("message", "No token provided");
                return response;
            }
            
            String token = authHeader.substring(7);
            System.out.println("Token received, length: " + token.length());
            
            boolean isValid = jwtUtil.validateToken(token);
            String username = jwtUtil.extractUsername(token);
            
            response.put("success", true);
            response.put("token_valid", isValid);
            response.put("username", username);
            response.put("token_length", token.length());
            
            System.out.println("✅ JWT Test completed");
            System.out.println("=================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ JWT Test error: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/users")
    public Map<String, Object> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("👥 Getting all users");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User u : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", u.getId());
                userMap.put("username", u.getUsername());
                userMap.put("email", u.getEmail());
                userMap.put("role", u.getRole().name());
                userMap.put("name", u.getName());
                userMap.put("createdAt", u.getCreatedAt());
                userList.add(userMap);
            }
            
            response.put("success", true);
            response.put("users", userList);
            response.put("count", users.size());
            
            System.out.println("✅ Retrieved " + users.size() + " users");
            
        } catch (Exception e) {
            System.out.println("❌ Error getting users: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/admin/stats")
    public Map<String, Object> getAdminStats(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 Getting admin stats");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<ComplaintDTO> complaints = complaintService.getAllComplaints();
            
            long totalComplaints = complaints.size();
            long openComplaints = complaints.stream()
                    .filter(c -> "OPEN".equals(c.getStatus()))
                    .count();
            long inProgressComplaints = complaints.stream()
                    .filter(c -> "IN_PROGRESS".equals(c.getStatus()))
                    .count();
            long resolvedComplaints = complaints.stream()
                    .filter(c -> "RESOLVED".equals(c.getStatus()))
                    .count();
            
            // Escalation stats
            long escalatedComplaints = complaints.stream()
                    .filter(c -> c.getEscalationLevel() != null && c.getEscalationLevel() > 0)
                    .count();
            
            response.put("success", true);
            response.put("stats", Map.of(
                "totalComplaints", totalComplaints,
                "openComplaints", openComplaints,
                "inProgressComplaints", inProgressComplaints,
                "resolvedComplaints", resolvedComplaints,
                "escalatedComplaints", escalatedComplaints,
                "usersCount", userRepository.count()
            ));
            
            System.out.println("✅ Admin stats sent to: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ Error getting admin stats: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/test-db")
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🗄️ Testing database connection");
            
            long userCount = userRepository.count();
            List<ComplaintDTO> complaints = complaintService.getAllComplaints();
            long complaintCount = complaints.size();
            
            response.put("success", true);
            response.put("database", "connected");
            response.put("users_count", userCount);
            response.put("complaints_count", complaintCount);
            response.put("message", "Database connection successful");
            
            System.out.println("✅ Database test successful");
            
        } catch (Exception e) {
            System.out.println("❌ Database test failed: " + e.getMessage());
            response.put("success", false);
            response.put("database", "error");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/debug-auth")
    public Map<String, Object> debugAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🔍 ========== DEBUG AUTH HEADER ==========");
            System.out.println("Full Authorization header: " + authHeader);
            
            if (authHeader == null) {
                response.put("success", false);
                response.put("message", "No Authorization header");
                return response;
            }
            
            if (!authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "Invalid format. Should start with 'Bearer '");
                response.put("header", authHeader);
                return response;
            }
            
            String token = authHeader.substring(7);
            System.out.println("Token extracted, length: " + token.length());
            
            boolean isValid = jwtUtil.validateToken(token);
            response.put("success", true);
            response.put("token_valid", isValid);
            response.put("token_length", token.length());
            
            if (isValid) {
                response.put("username", jwtUtil.extractUsername(token));
            }
            
            System.out.println("✅ Debug completed");
            System.out.println("===================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Debug error: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/debug/file/{complaintId}")
    public Map<String, Object> debugFileInfo(@PathVariable Long complaintId, 
                                            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🔍 ========== DEBUG FILE INFO ==========");
            System.out.println("Complaint ID: " + complaintId);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            System.out.println("User: " + username);
            
            Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
            if (complaintOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Complaint not found");
                return response;
            }
            
            Complaint complaint = complaintOpt.get();
            System.out.println("Complaint Title: " + complaint.getTitle());
            System.out.println("File Name in DB: " + complaint.getFileName());
            System.out.println("File Path in DB: " + complaint.getFilePath());
            
            response.put("success", true);
            response.put("fileExists", complaint.getFilePath() != null && Files.exists(Paths.get(complaint.getFilePath())));
            response.put("fileName", complaint.getFileName());
            response.put("filePath", complaint.getFilePath());
            
        } catch (Exception e) {
            System.out.println("❌ Debug error: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    // ================== ESCALATION ENDPOINTS ==================
    
    @GetMapping("/admin/escalation/complaints")
    public Map<String, Object> getEscalatedComplaints(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📈 Getting escalated complaints");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<ComplaintDTO> escalatedComplaints = complaintService.getEscalatedComplaints();
            response.put("success", true);
            response.put("data", escalatedComplaints);
            response.put("count", escalatedComplaints.size());
            
            System.out.println("✅ Retrieved " + escalatedComplaints.size() + " escalated complaints");
            
        } catch (Exception e) {
            System.out.println("❌ Error getting escalated complaints: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/admin/escalation/pending")
    public Map<String, Object> getPendingEscalationComplaints(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("⏳ Getting pending escalation complaints");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<ComplaintDTO> pendingComplaints = complaintService.getPendingEscalationComplaints();
            response.put("success", true);
            response.put("data", pendingComplaints);
            response.put("count", pendingComplaints.size());
            
            System.out.println("✅ Retrieved " + pendingComplaints.size() + " pending escalation complaints");
            
        } catch (Exception e) {
            System.out.println("❌ Error getting pending escalation complaints: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/admin/escalation/high-priority")
    public Map<String, Object> getHighPriorityComplaints(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("⚠️ Getting high priority complaints");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<ComplaintDTO> highPriority = complaintService.getHighPriorityComplaints();
            response.put("success", true);
            response.put("data", highPriority);
            response.put("count", highPriority.size());
            
            System.out.println("✅ Retrieved " + highPriority.size() + " high priority complaints");
            
        } catch (Exception e) {
            System.out.println("❌ Error getting high priority complaints: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // ================== TEST ESCALATION ENDPOINTS ==================
    
    @PostMapping("/admin/escalation/test-trigger")
    public Map<String, Object> testTriggerEscalation(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n🚀 ========== TEST TRIGGER ESCALATION ==========");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            // Trigger escalation check
            escalationService.checkAndEscalateComplaints();
            
            response.put("success", true);
            response.put("message", "Escalation check triggered successfully");
            response.put("timestamp", new Date());
            
            System.out.println("✅ Escalation test triggered by admin: " + username);
            System.out.println("==============================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Error triggering escalation: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/admin/escalation/manual/{complaintId}")
    public Map<String, Object> manualEscalateComplaint(
            @PathVariable Long complaintId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("\n👨‍💼 ========== MANUAL ESCALATION ==========");
            System.out.println("Complaint ID: " + complaintId);
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            Integer targetLevel = request.get("targetLevel") != null ? Integer.parseInt(request.get("targetLevel")) : 1;
            String reason = request.get("reason") != null ? request.get("reason") : "Manual escalation by admin";
            
            System.out.println("Target Level: " + targetLevel);
            System.out.println("Reason: " + reason);
            
            // Manual escalation
            ComplaintDTO escalatedComplaint = complaintService.manuallyEscalateComplaint(complaintId, targetLevel, reason);
            
            response.put("success", true);
            response.put("message", "Complaint manually escalated to level " + targetLevel);
            response.put("data", escalatedComplaint);
            
            System.out.println("✅ Manual escalation completed for complaint: " + complaintId);
            System.out.println("==================================================\n");
            
        } catch (Exception e) {
            System.out.println("❌ Error in manual escalation: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/admin/escalation/stats")
    public Map<String, Object> getEscalationStats(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 Getting escalation statistics");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            Map<String, Object> stats = escalationService.getEscalationStats();
            
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Escalation statistics retrieved");
            
            System.out.println("✅ Escalation stats sent to admin: " + username);
            
        } catch (Exception e) {
            System.out.println("❌ Error getting escalation stats: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/admin/escalation/config")
    public Map<String, Object> getEscalationConfig(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("⚙️ Getting escalation configuration");
            
            String token = validateAndExtractToken(authHeader);
            String username = jwtUtil.extractUsername(token);
            
            User user = authService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != Role.ADMIN) {
                response.put("success", false);
                response.put("message", "Admin access required");
                return response;
            }
            
            List<EscalationConfig> configs = escalationService.getAllEscalationConfigs();
            
            response.put("success", true);
            response.put("data", configs);
            response.put("count", configs.size());
            
            System.out.println("✅ Retrieved " + configs.size() + " escalation configs");
            
        } catch (Exception e) {
            System.out.println("❌ Error getting escalation config: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    // ================== PRIVATE HELPER METHODS ==================
    
    private boolean isValidCommentType(String type) {
        return type.equals("PUBLIC") || type.equals("INTERNAL");
    }
    
    private String validateAndExtractToken(String authHeader) {
        try {
            System.out.println("🔍 Validating Authorization header...");
            
            if (authHeader == null) {
                throw new RuntimeException("No Authorization header");
            }
            
            if (!authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid Authorization format. Should start with 'Bearer '");
            }
            
            String token = authHeader.substring(7);
            System.out.println("🔑 Token extracted, length: " + token.length());
            
            boolean isValid = jwtUtil.validateToken(token);
            if (!isValid) {
                throw new RuntimeException("Token validation failed");
            }
            
            System.out.println("✅ Token validated successfully");
            return token;
            
        } catch (Exception e) {
            System.out.println("❌ Token validation error: " + e.getMessage());
            throw new RuntimeException("Token error: " + e.getMessage());
        }
    }
    
    private boolean isValidPriority(String priority) {
        return priority.equals("LOW") || priority.equals("MEDIUM") || priority.equals("HIGH");
    }
}