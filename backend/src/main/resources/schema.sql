-- PathPilot AI Database Schema (MySQL 8.0 & H2 MySQL-mode Compatible)
-- Non-destructive, idempotent initialization preserving existing user data

-- 1. Students Profile Table
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL DEFAULT '',
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    target_role VARCHAR(100) NOT NULL,
    weekly_hours_available INT NOT NULL DEFAULT 15,
    max_budget DECIMAL(10, 2) NOT NULL DEFAULT 100.00,
    preferred_difficulty VARCHAR(30) NOT NULL DEFAULT 'INTERMEDIATE',
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Skills Catalog
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(60) NOT NULL,
    domain VARCHAR(60) NOT NULL,
    description TEXT
);

-- 3. Student Skills with Proficiency
CREATE TABLE IF NOT EXISTS student_skills (
    student_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level VARCHAR(30) NOT NULL,
    years_experience DECIMAL(4, 1) DEFAULT 0.0,
    is_verified BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (student_id, skill_id),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

-- 4. Target Role Skills Mapping
CREATE TABLE IF NOT EXISTS target_role_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    skill_id BIGINT NOT NULL,
    minimum_proficiency VARCHAR(30) NOT NULL,
    importance_weight DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    is_core BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_role_skill UNIQUE (role_name, skill_id),
    CONSTRAINT fk_role_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    INDEX idx_role_skills_role (role_name)
);

-- 5. Base Opportunities Table (Polymorphic Root)
CREATE TABLE IF NOT EXISTS opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    opportunity_type VARCHAR(40) NOT NULL,
    difficulty_level VARCHAR(30) NOT NULL,
    estimated_hours DECIMAL(6, 1) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    provider VARCHAR(150) NOT NULL,
    url VARCHAR(500),
    rating DECIMAL(3, 2) DEFAULT 4.50,
    rating_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_opp_type (opportunity_type),
    INDEX idx_opp_difficulty (difficulty_level),
    INDEX idx_opp_cost (cost)
);

-- 6. Specialized Opportunity Extension Tables
CREATE TABLE IF NOT EXISTS course_details (
    opportunity_id BIGINT PRIMARY KEY,
    platform VARCHAR(100) NOT NULL,
    syllabus_summary TEXT,
    certificate_included BOOLEAN DEFAULT TRUE,
    duration_weeks INT NOT NULL,
    CONSTRAINT fk_course_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS project_details (
    opportunity_id BIGINT PRIMARY KEY,
    repository_template_url VARCHAR(500),
    tech_stack_csv VARCHAR(255) NOT NULL,
    project_tier VARCHAR(40) NOT NULL,
    deliverable_description TEXT,
    CONSTRAINT fk_project_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS internship_details (
    opportunity_id BIGINT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    monthly_stipend DECIMAL(10, 2) DEFAULT 0.00,
    location_type VARCHAR(40) NOT NULL,
    duration_months INT NOT NULL,
    application_deadline VARCHAR(30),
    CONSTRAINT fk_internship_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS certification_details (
    opportunity_id BIGINT PRIMARY KEY,
    issuing_organization VARCHAR(150) NOT NULL,
    exam_code VARCHAR(50),
    validity_years INT DEFAULT 3,
    exam_format VARCHAR(100),
    CONSTRAINT fk_cert_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

-- 7. Opportunity Skills Mapping
CREATE TABLE IF NOT EXISTS opportunity_skills (
    opportunity_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    relationship_type VARCHAR(30) NOT NULL,
    target_proficiency VARCHAR(30) NOT NULL,
    PRIMARY KEY (opportunity_id, skill_id, relationship_type),
    CONSTRAINT fk_opp_skills_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    CONSTRAINT fk_opp_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

-- 8. Recommendations History
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    strategy_used VARCHAR(80) NOT NULL,
    CONSTRAINT fk_rec_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recommendation_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recommendation_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    ranking_score DECIMAL(5, 4) NOT NULL,
    content_score DECIMAL(5, 4) NOT NULL,
    skill_score DECIMAL(5, 4) NOT NULL,
    contextual_score DECIMAL(5, 4) NOT NULL,
    feedback_score DECIMAL(5, 4) NOT NULL,
    rank_order INT NOT NULL,
    explanation_json TEXT NOT NULL,
    CONSTRAINT fk_rec_item_rec FOREIGN KEY (recommendation_id) REFERENCES recommendations(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_item_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

-- 9. User Feedback Table
CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    action_type VARCHAR(40) NOT NULL,
    rating_value INT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    INDEX idx_feedback_student_opp (student_id, opportunity_id)
);

-- 10. Learning Plan & Roadmap Items
CREATE TABLE IF NOT EXISTS learning_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    sequence_order INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    target_completion_weeks INT NOT NULL,
    prerequisite_note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    INDEX idx_plans_student (student_id, status)
);
