package com.grievance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    private Key key;
    
    @PostConstruct
    public void init() {
        System.out.println("🔑 JWT Configuration:");
        System.out.println("   Secret length: " + (secret != null ? secret.length() : 0));
        System.out.println("   Expiration: " + expiration + "ms (" + (expiration / 1000 / 60 / 60) + " hours)");
        
        // Convert secret to key
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("❌ JWT Parsing Error: " + e.getMessage());
            throw new RuntimeException("Invalid JWT token: " + e.getMessage());
        }
    }
    
    public boolean validateToken(String token) {
        try {
            System.out.println("🔍 Validating token...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            boolean isExpired = claims.getExpiration().before(new Date());
            if (isExpired) {
                System.out.println("❌ Token expired at: " + claims.getExpiration());
                return false;
            }
            
            System.out.println("✅ Token valid for user: " + claims.getSubject());
            System.out.println("   Expires at: " + claims.getExpiration());
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ Token validation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }
    
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        System.out.println("🔐 Generating JWT token for: " + subject);
        System.out.println("   Issued at: " + now);
        System.out.println("   Expires at: " + expiryDate);
        System.out.println("   Secret key length: " + secret.length());
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        System.out.println("✅ Token generated successfully");
        System.out.println("   Token length: " + token.length());
        
        return token;
    }
}