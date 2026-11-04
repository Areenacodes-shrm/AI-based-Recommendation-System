-- PathPilot AI Seed Data
-- Comprehensive realistic seed dataset for student opportunity recommendation and skill gap analysis

-- 1. Canonical Skills
INSERT INTO skills (id, name, category, domain, description) VALUES
(1, 'Java', 'LANGUAGE', 'Backend', 'Core Java programming language including OOP, Generics, and Collections'),
(2, 'Spring Boot', 'FRAMEWORK', 'Backend', 'Enterprise Java framework for REST APIs, Dependency Injection, and Microservices'),
(3, 'SQL', 'DATABASE', 'Data & Storage', 'Relational database querying, indexing, transactions, and schema design'),
(4, 'JDBC', 'DATABASE', 'Backend', 'Java Database Connectivity API for raw SQL execution and transaction management'),
(5, 'Multithreading', 'ARCHITECTURE', 'Systems', 'Concurrent execution, thread pools, synchronization, and race condition mitigation'),
(6, 'Docker', 'DEVOPS', 'Infrastructure', 'Containerization for packaging and deploying portable microservices'),
(7, 'REST APIs', 'ARCHITECTURE', 'Backend', 'Architectural style for designing networked web APIs with JSON payloads'),
(8, 'Data Structures', 'FOUNDATION', 'Computer Science', 'Arrays, linked lists, trees, graphs, heaps, and algorithmic time complexity'),
(9, 'System Design', 'ARCHITECTURE', 'Distributed Systems', 'Designing scalable, fault-tolerant, high-availability distributed systems'),
(10, 'Python', 'LANGUAGE', 'Data & AI', 'High-level programming language widely used in AI, ML, and automation'),
(11, 'Pandas', 'LIBRARY', 'Data Analysis', 'Data manipulation and analysis library for structured tabular data'),
(12, 'Machine Learning', 'AI_ML', 'Artificial Intelligence', 'Supervised, unsupervised, regression, classification, and ensemble techniques'),
(13, 'Deep Learning', 'AI_ML', 'Artificial Intelligence', 'Neural networks, convolutional networks, transformers, and gradient descent'),
(14, 'Git', 'TOOL', 'Software Engineering', 'Distributed version control, branching workflows, and pull requests'),
(15, 'Unit Testing', 'METHODOLOGY', 'Software Engineering', 'Writing automated unit tests using JUnit, Mockito, and TDD practices'),
(16, 'Data Visualization', 'VISUALIZATION', 'Data Analysis', 'Communicating quantitative insights using charts, dashboards, and plots'),
(17, 'Microservices', 'ARCHITECTURE', 'Backend', 'Decomposing monolithic applications into loosely coupled autonomous services');

-- 2. Target Role Skills Mapping
-- Role: Java Backend Developer
INSERT INTO target_role_skills (role_name, skill_id, minimum_proficiency, importance_weight, is_core) VALUES
('Java Backend Developer', 1, 'ADVANCED', 1.00, TRUE),
('Java Backend Developer', 2, 'INTERMEDIATE', 0.95, TRUE),
('Java Backend Developer', 3, 'ADVANCED', 0.90, TRUE),
('Java Backend Developer', 4, 'INTERMEDIATE', 0.85, TRUE),
('Java Backend Developer', 5, 'INTERMEDIATE', 0.85, TRUE),
('Java Backend Developer', 7, 'ADVANCED', 0.80, TRUE),
('Java Backend Developer', 8, 'ADVANCED', 0.75, TRUE),
('Java Backend Developer', 6, 'BEGINNER', 0.60, FALSE),
('Java Backend Developer', 15, 'INTERMEDIATE', 0.70, TRUE),
('Java Backend Developer', 17, 'BEGINNER', 0.65, FALSE);

-- Role: Software Engineer
INSERT INTO target_role_skills (role_name, skill_id, minimum_proficiency, importance_weight, is_core) VALUES
('Software Engineer', 1, 'INTERMEDIATE', 0.90, TRUE),
('Software Engineer', 8, 'ADVANCED', 1.00, TRUE),
('Software Engineer', 3, 'INTERMEDIATE', 0.80, TRUE),
('Software Engineer', 7, 'INTERMEDIATE', 0.85, TRUE),
('Software Engineer', 14, 'INTERMEDIATE', 0.75, TRUE),
('Software Engineer', 15, 'INTERMEDIATE', 0.80, TRUE),
('Software Engineer', 9, 'BEGINNER', 0.70, FALSE);

-- Role: Data Analyst
INSERT INTO target_role_skills (role_name, skill_id, minimum_proficiency, importance_weight, is_core) VALUES
('Data Analyst', 3, 'ADVANCED', 1.00, TRUE),
('Data Analyst', 10, 'INTERMEDIATE', 0.90, TRUE),
('Data Analyst', 11, 'ADVANCED', 0.95, TRUE),
('Data Analyst', 16, 'ADVANCED', 0.90, TRUE),
('Data Analyst', 14, 'BEGINNER', 0.60, FALSE);

-- Role: AI/ML Engineer
INSERT INTO target_role_skills (role_name, skill_id, minimum_proficiency, importance_weight, is_core) VALUES
('AI/ML Engineer', 10, 'ADVANCED', 1.00, TRUE),
('AI/ML Engineer', 12, 'ADVANCED', 1.00, TRUE),
('AI/ML Engineer', 13, 'INTERMEDIATE', 0.90, TRUE),
('AI/ML Engineer', 11, 'INTERMEDIATE', 0.85, TRUE),
('AI/ML Engineer', 8, 'INTERMEDIATE', 0.80, TRUE),
('AI/ML Engineer', 6, 'BEGINNER', 0.65, FALSE);

-- 3. Base Opportunities Catalog
INSERT INTO opportunities (id, title, description, opportunity_type, difficulty_level, estimated_hours, cost, provider, url, rating, rating_count) VALUES
-- Courses
(1, 'Modern Java 21 & Concurrency Masterclass', 'Deep dive into Java 21 virtual threads, structured concurrency, generics, and high-performance collections.', 'COURSE', 'INTERMEDIATE', 45.0, 39.99, 'JavaAcademy', 'https://javaacademy.dev/courses/java21', 4.85, 230),
(2, 'Spring Boot 3 Enterprise Microservices', 'Architect production-grade enterprise microservices with Spring Boot 3, Spring Data JDBC, Security, and Docker.', 'COURSE', 'ADVANCED', 60.0, 59.99, 'CloudEnterprise Labs', 'https://cloudlabs.dev/spring-boot-3', 4.90, 412),
(3, 'SQL Performance Tuning & Relational Modeling', 'Master query execution plans, indexing strategies, ACID transactions, and high-throughput schema design.', 'COURSE', 'INTERMEDIATE', 30.0, 29.99, 'DataCraft', 'https://datacraft.org/sql-tuning', 4.75, 180),
(4, 'Applied Data Analysis with Python & Pandas', 'Hands-on exploration of real-world datasets, statistical summarization, data wrangling, and interactive dashboards.', 'COURSE', 'BEGINNER', 35.0, 19.99, 'DataCampus', 'https://datacampus.org/python-pandas', 4.70, 520),
(5, 'Deep Learning & Neural Architectures', 'Comprehensive foundation covering CNNs, RNNs, Transformers, backpropagation calculus, and PyTorch deployment.', 'COURSE', 'ADVANCED', 70.0, 79.99, 'AI University', 'https://aiuniv.org/deep-learning', 4.88, 310),

-- Projects
(6, 'High-Throughput Concurrent Cache Engine', 'Build a production-ready, lock-striped LRU memory cache in Java with generic key-value store, expiration, and benchmarks.', 'PROJECT', 'ADVANCED', 25.0, 0.00, 'GitHub OpenSource Incubator', 'https://github.com/incubator/concurrent-cache', 4.92, 95),
(7, 'Distributed Order Processing System', 'Implement an event-driven inventory and order processing system using Spring Boot, JDBC, and transaction management.', 'PROJECT', 'INTERMEDIATE', 40.0, 0.00, 'OpenDev Collective', 'https://github.com/opendev/order-processor', 4.80, 145),
(8, 'Real-Time Financial Dashboard with Pandas', 'Build a live financial portfolio analytics dashboard computing risk metrics, Sharpe ratio, and trend charts.', 'PROJECT', 'INTERMEDIATE', 20.0, 0.00, 'FinData Hub', 'https://github.com/findata/portfolio-analyzer', 4.65, 88),
(9, 'Chest X-Ray Pathology Classifier', 'Train an explainable vision transformer model with Grad-CAM visual heatmaps for diagnostic radiograph interpretation.', 'PROJECT', 'ADVANCED', 50.0, 0.00, 'HealthAI Labs', 'https://github.com/healthai/xray-classifier', 4.91, 112),

-- Internships
(10, 'Backend Systems Engineering Intern', 'Join an agile engineering squad building resilient Java microservices, API integrations, and database optimizations.', 'INTERNSHIP', 'INTERMEDIATE', 160.0, 0.00, 'FinTech Cloud Technologies', 'https://fintechcloud.com/careers/intern-backend', 4.85, 34),
(11, 'Junior Data & Analytics Intern', 'Collaborate with product analytics team to write SQL extraction queries, build automated KPI dashboards, and clean data.', 'INTERNSHIP', 'BEGINNER', 120.0, 0.00, 'Global Logistics Corp', 'https://globallogistics.com/interns', 4.60, 21),
(12, 'Applied AI Research Intern', 'Develop and benchmark computer vision and NLP models on proprietary datasets in a hybrid research environment.', 'INTERNSHIP', 'ADVANCED', 200.0, 0.00, 'Nexus AI Systems', 'https://nexusai.io/careers/research-intern', 4.94, 18),

-- Certifications
(13, 'Oracle Certified Professional: Java SE 21 Developer', 'Globally recognized gold standard certification verifying deep mastery of Java language features, JVM internals, and APIs.', 'CERTIFICATION', 'ADVANCED', 120.0, 245.00, 'Oracle University', 'https://oracle.com/certification/java-se-21', 4.95, 680),
(14, 'AWS Certified Solutions Architect - Associate', 'Validates expertise in designing resilient, cost-effective, high-performing cloud architectures on Amazon Web Services.', 'CERTIFICATION', 'INTERMEDIATE', 80.0, 150.00, 'Amazon Web Services', 'https://aws.amazon.com/certification/solutions-architect', 4.88, 1250);

-- 4. Course Details
INSERT INTO course_details (opportunity_id, platform, syllabus_summary, certificate_included, duration_weeks) VALUES
(1, 'JavaAcademy', 'Module 1: Java 21 Syntax & Records. Module 2: Virtual Threads & Concurrency. Module 3: Advanced Generics & Collections. Module 4: Memory Optimization.', TRUE, 6),
(2, 'CloudEnterprise Labs', 'Module 1: Spring Boot Architecture. Module 2: REST APIs & JDBC. Module 3: Security & OAuth2. Module 4: Docker & CI/CD Pipelines.', TRUE, 8),
(3, 'DataCraft', 'Module 1: Relational Algebra & SQL. Module 2: B-Tree Indexing. Module 3: Query Optimization Plans. Module 4: Transactions & Isolation Levels.', TRUE, 4),
(4, 'DataCampus', 'Module 1: Python Fundamentals. Module 2: Pandas Series & DataFrames. Module 3: Data Cleaning & Imputation. Module 4: Exploratory Analysis.', TRUE, 5),
(5, 'AI University', 'Module 1: Deep Feedforward Networks. Module 2: Convolutional Networks. Module 3: Transformers & Attention. Module 4: Model Serving & Quantization.', TRUE, 10);

-- 5. Project Details
INSERT INTO project_details (opportunity_id, repository_template_url, tech_stack_csv, project_tier, deliverable_description) VALUES
(6, 'https://github.com/incubator/concurrent-cache', 'Java,Multithreading,JUnit 5,JMH', 'CAPSTONE', 'A thread-safe generic cache library with eviction policies, concurrency benchmarks, and comprehensive unit tests.'),
(7, 'https://github.com/opendev/order-processor', 'Java,Spring Boot,JDBC,Docker,MySQL', 'GUIDED', 'A complete backend service processing orders, managing stock locks, and recording transactions with rollback handling.'),
(8, 'https://github.com/findata/portfolio-analyzer', 'Python,Pandas,Data Visualization,REST APIs', 'GUIDED', 'A clean Python dashboard reading market quotes, computing rolling variance, and rendering interactive performance plots.'),
(9, 'https://github.com/healthai/xray-classifier', 'Python,Deep Learning,Machine Learning,Docker', 'OPEN_SOURCE', 'A trained PyTorch deep convolutional pipeline with attention maps showing diagnostic explainability for radiologist review.');

-- 6. Internship Details
INSERT INTO internship_details (opportunity_id, company_name, monthly_stipend, location_type, duration_months, application_deadline) VALUES
(10, 'FinTech Cloud Technologies', 1800.00, 'REMOTE', 3, '2026-12-15'),
(11, 'Global Logistics Corp', 1200.00, 'HYBRID', 3, '2026-11-30'),
(12, 'Nexus AI Systems', 2400.00, 'REMOTE', 4, '2026-12-01');

-- 7. Certification Details
INSERT INTO certification_details (opportunity_id, issuing_organization, exam_code, validity_years, exam_format) VALUES
(13, 'Oracle University', '1Z0-830', 5, 'Proctored Multiple Choice & Scenario Questions (50 questions, 120 min)'),
(14, 'Amazon Web Services', 'SAA-C03', 3, 'Proctored Multiple Choice & Multi-Response (65 questions, 130 min)');

-- 8. Opportunity Skills Mapping (Prerequisites & Gained)
-- Opp 1: Modern Java 21 & Concurrency
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(1, 1, 'PREREQUISITE', 'BEGINNER'),
(1, 1, 'GAINED', 'ADVANCED'),
(1, 5, 'GAINED', 'INTERMEDIATE'),
(1, 8, 'GAINED', 'INTERMEDIATE');

-- Opp 2: Spring Boot 3 Enterprise Microservices
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(2, 1, 'PREREQUISITE', 'INTERMEDIATE'),
(2, 3, 'PREREQUISITE', 'BEGINNER'),
(2, 2, 'GAINED', 'ADVANCED'),
(2, 4, 'GAINED', 'INTERMEDIATE'),
(2, 7, 'GAINED', 'ADVANCED'),
(2, 6, 'GAINED', 'INTERMEDIATE'),
(2, 17, 'GAINED', 'INTERMEDIATE');

-- Opp 3: SQL Performance Tuning
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(3, 3, 'PREREQUISITE', 'BEGINNER'),
(3, 3, 'GAINED', 'ADVANCED');

-- Opp 4: Applied Data Analysis with Python
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(4, 10, 'GAINED', 'INTERMEDIATE'),
(4, 11, 'GAINED', 'ADVANCED'),
(4, 16, 'GAINED', 'INTERMEDIATE');

-- Opp 5: Deep Learning
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(5, 10, 'PREREQUISITE', 'INTERMEDIATE'),
(5, 12, 'PREREQUISITE', 'INTERMEDIATE'),
(5, 13, 'GAINED', 'ADVANCED');

-- Opp 6: High-Throughput Concurrent Cache
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(6, 1, 'PREREQUISITE', 'INTERMEDIATE'),
(6, 5, 'PREREQUISITE', 'BEGINNER'),
(6, 5, 'GAINED', 'ADVANCED'),
(6, 15, 'GAINED', 'ADVANCED'),
(6, 8, 'GAINED', 'ADVANCED');

-- Opp 7: Distributed Order Processing
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(7, 1, 'PREREQUISITE', 'BEGINNER'),
(7, 2, 'GAINED', 'INTERMEDIATE'),
(7, 4, 'GAINED', 'INTERMEDIATE'),
(7, 7, 'GAINED', 'INTERMEDIATE');

-- Opp 8: Real-Time Financial Dashboard
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(8, 10, 'PREREQUISITE', 'BEGINNER'),
(8, 11, 'GAINED', 'ADVANCED'),
(8, 16, 'GAINED', 'ADVANCED');

-- Opp 9: Chest X-Ray Pathology Classifier
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(9, 10, 'PREREQUISITE', 'INTERMEDIATE'),
(9, 12, 'PREREQUISITE', 'INTERMEDIATE'),
(9, 13, 'GAINED', 'ADVANCED'),
(9, 15, 'GAINED', 'INTERMEDIATE');

-- Opp 10: Backend Systems Engineering Intern
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(10, 1, 'PREREQUISITE', 'INTERMEDIATE'),
(10, 3, 'PREREQUISITE', 'INTERMEDIATE'),
(10, 7, 'GAINED', 'ADVANCED'),
(10, 17, 'GAINED', 'INTERMEDIATE');

-- Opp 11: Junior Data & Analytics Intern
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(11, 3, 'PREREQUISITE', 'BEGINNER'),
(11, 10, 'PREREQUISITE', 'BEGINNER'),
(11, 11, 'GAINED', 'ADVANCED'),
(11, 16, 'GAINED', 'ADVANCED');

-- Opp 12: Applied AI Research Intern
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(12, 10, 'PREREQUISITE', 'INTERMEDIATE'),
(12, 12, 'PREREQUISITE', 'ADVANCED'),
(12, 13, 'GAINED', 'ADVANCED');

-- Opp 13: Oracle Certified Professional: Java SE 21
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(13, 1, 'PREREQUISITE', 'INTERMEDIATE'),
(13, 1, 'GAINED', 'EXPERT'),
(13, 5, 'GAINED', 'ADVANCED');

-- Opp 14: AWS Solutions Architect
INSERT INTO opportunity_skills (opportunity_id, skill_id, relationship_type, target_proficiency) VALUES
(14, 9, 'GAINED', 'ADVANCED'),
(14, 6, 'GAINED', 'ADVANCED');

-- 9. Sample Students
INSERT INTO students (id, full_name, email, target_role, weekly_hours_available, max_budget, preferred_difficulty, bio) VALUES
(1, 'Alex Rivera', 'alex.rivera@university.edu', 'Java Backend Developer', 18, 150.00, 'INTERMEDIATE', 'Junior CS student passionate about distributed systems, backend concurrency, and clean enterprise software.'),
(2, 'Priya Sharma', 'priya.sharma@tech.edu', 'AI/ML Engineer', 20, 200.00, 'ADVANCED', 'Final-year engineering student focusing on neural models, deep computer vision, and applied ML pipelines.'),
(3, 'Marcus Chen', 'marcus.chen@college.edu', 'Data Analyst', 12, 50.00, 'BEGINNER', 'Economics and informatics student keen on relational databases, visual storytelling, and business analytics.');

-- 10. Student Skills
-- Alex Rivera: knows Java (Intermediate), SQL (Beginner), Git (Intermediate)
INSERT INTO student_skills (student_id, skill_id, proficiency_level, years_experience, is_verified) VALUES
(1, 1, 'INTERMEDIATE', 1.5, TRUE),
(1, 3, 'BEGINNER', 0.5, FALSE),
(1, 14, 'INTERMEDIATE', 1.0, TRUE);

-- Priya Sharma: knows Python (Advanced), Machine Learning (Intermediate), Data Structures (Intermediate)
INSERT INTO student_skills (student_id, skill_id, proficiency_level, years_experience, is_verified) VALUES
(2, 10, 'ADVANCED', 2.0, TRUE),
(2, 12, 'INTERMEDIATE', 1.0, TRUE),
(2, 8, 'INTERMEDIATE', 1.5, FALSE);

-- Marcus Chen: knows SQL (Beginner), Python (Beginner)
INSERT INTO student_skills (student_id, skill_id, proficiency_level, years_experience, is_verified) VALUES
(3, 3, 'BEGINNER', 0.3, FALSE),
(3, 10, 'BEGINNER', 0.5, FALSE);

-- 11. Initial User Feedback
INSERT INTO user_feedback (student_id, opportunity_id, action_type, rating_value, notes) VALUES
(1, 1, 'SAVED', NULL, 'Looks like an outstanding concurrency course with virtual threads.'),
(1, 7, 'RATED', 5, 'Great practical Spring Boot project with transactions.');

-- 12. Initial Learning Plan for Alex Rivera
INSERT INTO learning_plans (student_id, opportunity_id, sequence_order, status, target_completion_weeks, prerequisite_note) VALUES
(1, 1, 1, 'IN_PROGRESS', 6, 'Builds strong Java 21 fundamentals for backend role'),
(1, 7, 2, 'PENDING', 4, 'Hands-on Spring Boot & JDBC order processing'),
(1, 2, 3, 'PENDING', 8, 'Advanced enterprise architecture and microservices');
