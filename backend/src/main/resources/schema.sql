-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create complaints table
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    user_id BIGINT,
    assigned_to VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
-- Add escalation columns to complaints table if they don't exist
ALTER TABLE complaints 
ADD COLUMN IF NOT EXISTS escalation_level INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS escalation_recipients VARCHAR(500) NULL,
ADD COLUMN IF NOT EXISTS next_escalation_time TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS escalation_notes TEXT NULL;

-- Create escalation_config table if not exists
CREATE TABLE IF NOT EXISTS escalation_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    level INT NOT NULL UNIQUE,
    time_limit_hours INT NOT NULL,
    assignee_role VARCHAR(100) NOT NULL,
    recipients VARCHAR(500) NOT NULL,
    active BOOLEAN DEFAULT true
);

-- Create escalation_history table if not exists
CREATE TABLE IF NOT EXISTS escalation_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    complaint_id INT NOT NULL,
    escalated_from VARCHAR(100),
    escalated_to VARCHAR(100),
    escalation_level INT,
    escalated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE
);

-- Clear any existing data and insert new configs with Super Admin
DELETE FROM escalation_config;

INSERT INTO escalation_config (level, time_limit_hours, assignee_role, recipients) VALUES
(1, 24, 'LEVEL1_SUPPORT', 'support@company.com'),
(2, 48, 'LEVEL2_SUPPORT', 'support@company.com,manager@company.com'),
(3, 72, 'MANAGER', 'manager@company.com,director@company.com'),
(4, 96, 'DIRECTOR', 'director@company.com,vp@company.com'),
(5, 120, 'SUPER_ADMIN', 'superadmin@company.com,ceo@company.com');