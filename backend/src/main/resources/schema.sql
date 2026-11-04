-- PathPilot AI Database Schema (MySQL 8.0 & Compatible)
-- Clean and idempotent initialization

DROP TABLE IF EXISTS learning_plans;
DROP TABLE IF EXISTS user_feedback;
DROP TABLE IF EXISTS recommendation_items;
DROP TABLE IF EXISTS recommendations;
DROP TABLE IF EXISTS opportunity_skills;
DROP TABLE IF EXISTS certification_details;
DROP TABLE IF EXISTS internship_details;
DROP TABLE IF EXISTS project_details;
DROP TABLE IF EXISTS course_details;
DROP TABLE IF EXISTS opportunities;
DROP TABLE IF EXISTS target_role_skills;
DROP TABLE IF EXISTS student_skills;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS students;

-- 1. Students Profile Table
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    target_role VARCHAR(100) NOT NULL,
    weekly_hours_available INT NOT NULL DEFAULT 15,
    max_budget DECIMAL(10, 2) NOT NULL DEFAULT 100.00,
    preferred_difficulty VARCHAR(30) NOT NULL DEFAULT 'INTERMEDIATE',
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Skills Catalog
CREATE TABLE skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(60) NOT NULL,
    domain VARCHAR(60) NOT NULL,
    description TEXT
);

-- 3. Student Skills with Proficiency
CREATE TABLE student_skills (
    student_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level VARCHAR(30) NOT NULL, -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    years_experience DECIMAL(4, 1) DEFAULT 0.0,
    is_verified BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (student_id, skill_id),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

-- 4. Target Role Skills Mapping
CREATE TABLE target_role_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    skill_id BIGINT NOT NULL,
    minimum_proficiency VARCHAR(30) NOT NULL,
    importance_weight DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    is_core BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_role_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

-- 5. Base Opportunities Table (Polymorphic Root)
CREATE TABLE opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    opportunity_type VARCHAR(40) NOT NULL, -- COURSE, PROJECT, INTERNSHIP, CERTIFICATION
    difficulty_level VARCHAR(30) NOT NULL, -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    estimated_hours DECIMAL(6, 1) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    provider VARCHAR(150) NOT NULL,
    url VARCHAR(500),
    rating DECIMAL(3, 2) DEFAULT 4.50,
    rating_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Specialized Opportunity Extension Tables
CREATE TABLE course_details (
    opportunity_id BIGINT PRIMARY KEY,
    platform VARCHAR(100) NOT NULL,
    syllabus_summary TEXT,
    certificate_included BOOLEAN DEFAULT TRUE,
    duration_weeks INT NOT NULL,
    CONSTRAINT fk_course_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE TABLE project_details (
    opportunity_id BIGINT PRIMARY KEY,
    repository_template_url VARCHAR(500),
    tech_stack_csv VARCHAR(255) NOT NULL,
    project_tier VARCHAR(40) NOT NULL, -- GUIDED, CAPSTONE, OPEN_SOURCE
    deliverable_description TEXT,
    CONSTRAINT fk_project_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE TABLE internship_details (
    opportunity_id BIGINT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    monthly_stipend DECIMAL(10, 2) DEFAULT 0.00,
    location_type VARCHAR(40) NOT NULL, -- REMOTE, HYBRID, ON_SITE
    duration_months INT NOT NULL,
    application_deadline VARCHAR(30),
    CONSTRAINT fk_internship_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

CREATE TABLE certification_details (
    opportunity_id BIGINT PRIMARY KEY,
    issuing_organization VARCHAR(150) NOT NULL,
    exam_code VARCHAR(50),
    validity_years INT DEFAULT 3,
    exam_format VARCHAR(100),
    CONSTRAINT fk_cert_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

-- 7. Opportunity Skills Mapping
CREATE TABLE opportunity_skills (
    opportunity_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    relationship_type VARCHAR(30) NOT NULL, -- PREREQUISITE, GAINED
    target_proficiency VARCHAR(30) NOT NULL,
    PRIMARY KEY (opportunity_id, skill_id, relationship_type),
    CONSTRAINT fk_opp_skills_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE,
    CONSTRAINT fk_opp_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
);

-- 8. Recommendations History
CREATE TABLE recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    strategy_used VARCHAR(80) NOT NULL,
    CONSTRAINT fk_rec_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE recommendation_items (
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
CREATE TABLE user_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    action_type VARCHAR(40) NOT NULL, -- SAVED, DISMISSED, RATED, COMPLETED
    rating_value INT, -- 1 to 5
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

-- 10. Learning Plan & Roadmap Items
CREATE TABLE learning_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    sequence_order INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    target_completion_weeks INT NOT NULL,
    prerequisite_note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_opp FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
);

-- Indexes for efficient queries
CREATE INDEX idx_student_email ON students(email);
CREATE INDEX idx_opp_type ON opportunities(opportunity_type);
CREATE INDEX idx_opp_difficulty ON opportunities(difficulty_level);
CREATE INDEX idx_opp_cost ON opportunities(cost);
CREATE INDEX idx_role_skills_role ON target_role_skills(role_name);
CREATE INDEX idx_feedback_student_opp ON user_feedback(student_id, opportunity_id);
CREATE INDEX idx_plans_student ON learning_plans(student_id, status);
