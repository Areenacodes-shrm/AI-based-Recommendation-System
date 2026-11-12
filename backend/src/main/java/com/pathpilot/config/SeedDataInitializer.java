package com.pathpilot.config;

import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.*;
import com.pathpilot.repository.*;
import com.pathpilot.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Idempotent, non-destructive startup data seeder using explicit JDBC and repositories.
 * Ensures reference catalog rows exist without duplicating or overwriting user data on restarts.
 */
@Component
@Order(1)
public class SeedDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final SkillRepository skillRepository;
    private final OpportunityRepository opportunityRepository;
    private final StudentRepository studentRepository;
    private final FeedbackRepository feedbackRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final boolean seedEnabled;
    private final String demoPassword;

    public SeedDataInitializer(
            JdbcTemplate jdbcTemplate,
            SkillRepository skillRepository,
            OpportunityRepository opportunityRepository,
            StudentRepository studentRepository,
            FeedbackRepository feedbackRepository,
            LearningPlanRepository learningPlanRepository,
            @Value("${pathpilot.seed.enabled:true}") boolean seedEnabled,
            @Value("${pathpilot.demo.password:PathPilotDemo!2026}") String demoPassword) {
        this.jdbcTemplate = jdbcTemplate;
        this.skillRepository = skillRepository;
        this.opportunityRepository = opportunityRepository;
        this.studentRepository = studentRepository;
        this.feedbackRepository = feedbackRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.seedEnabled = seedEnabled;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAll();
    }

    /**
     * Executes idempotent reference catalog and initial demo persona seeding.
     * Safe to invoke repeatedly across restarts without creating duplicate rows
     * or mutating existing user profiles, feedback, or roadmaps.
     */
    @Transactional
    public void seedAll() {
        if (!seedEnabled) {
            log.info("Startup seed initializer is disabled via configuration.");
            return;
        }

        Map<String, Skill> skillsByName = ensureSkills();
        ensureTargetRoleSkills(skillsByName);
        Map<String, Opportunity> opportunitiesByTitle = ensureOpportunities(skillsByName);
        ensureInitialDemoStudents(skillsByName, opportunitiesByTitle);
    }

    private Map<String, Skill> ensureSkills() {
        List<Skill> catalog = List.of(
                new Skill(null, "Java", "LANGUAGE", "Backend", "Core Java programming language including OOP, Generics, and Collections"),
                new Skill(null, "Spring Boot", "FRAMEWORK", "Backend", "Enterprise Java framework for REST APIs, Dependency Injection, and Microservices"),
                new Skill(null, "SQL", "DATABASE", "Data & Storage", "Relational database querying, indexing, transactions, and schema design"),
                new Skill(null, "JDBC", "DATABASE", "Backend", "Java Database Connectivity API for raw SQL execution and transaction management"),
                new Skill(null, "Multithreading", "ARCHITECTURE", "Systems", "Concurrent execution, thread pools, synchronization, and race condition mitigation"),
                new Skill(null, "Docker", "DEVOPS", "Infrastructure", "Containerization for packaging and deploying portable microservices"),
                new Skill(null, "REST APIs", "ARCHITECTURE", "Backend", "Architectural style for designing networked web APIs with JSON payloads"),
                new Skill(null, "Data Structures", "FOUNDATION", "Computer Science", "Arrays, linked lists, trees, graphs, heaps, and algorithmic time complexity"),
                new Skill(null, "System Design", "ARCHITECTURE", "Distributed Systems", "Designing scalable, fault-tolerant, high-availability distributed systems"),
                new Skill(null, "Python", "LANGUAGE", "Data & AI", "High-level programming language widely used in AI, ML, and automation"),
                new Skill(null, "Pandas", "LIBRARY", "Data Analysis", "Data manipulation and analysis library for structured tabular data"),
                new Skill(null, "Machine Learning", "AI_ML", "Artificial Intelligence", "Supervised, unsupervised, regression, classification, and ensemble techniques"),
                new Skill(null, "Deep Learning", "AI_ML", "Artificial Intelligence", "Neural networks, convolutional networks, transformers, and gradient descent"),
                new Skill(null, "Git", "TOOL", "Software Engineering", "Distributed version control, branching workflows, and pull requests"),
                new Skill(null, "Unit Testing", "METHODOLOGY", "Software Engineering", "Writing automated unit tests using JUnit, Mockito, and TDD practices"),
                new Skill(null, "Data Visualization", "VISUALIZATION", "Data Analysis", "Communicating quantitative insights using charts, dashboards, and plots"),
                new Skill(null, "Microservices", "ARCHITECTURE", "Backend", "Decomposing monolithic applications into loosely coupled autonomous services")
        );

        Map<String, Skill> resolved = new HashMap<>();
        for (Skill existing : skillRepository.findAll()) {
            resolved.put(existing.getName().toLowerCase(), existing);
        }

        for (Skill candidate : catalog) {
            String key = candidate.getName().toLowerCase();
            if (!resolved.containsKey(key)) {
                Skill saved = skillRepository.save(candidate);
                resolved.put(key, saved);
            }
        }
        return resolved;
    }

    private void ensureTargetRoleSkills(Map<String, Skill> skills) {
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("java"), "ADVANCED", 1.00, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("spring boot"), "INTERMEDIATE", 0.95, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("sql"), "ADVANCED", 0.90, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("jdbc"), "INTERMEDIATE", 0.85, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("multithreading"), "INTERMEDIATE", 0.85, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("rest apis"), "ADVANCED", 0.80, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("data structures"), "ADVANCED", 0.75, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("docker"), "BEGINNER", 0.60, false);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("unit testing"), "INTERMEDIATE", 0.70, true);
        insertRoleSkillIfMissing("Java Backend Developer", skills.get("microservices"), "BEGINNER", 0.65, false);

        insertRoleSkillIfMissing("Software Engineer", skills.get("java"), "INTERMEDIATE", 0.90, true);
        insertRoleSkillIfMissing("Software Engineer", skills.get("data structures"), "ADVANCED", 1.00, true);
        insertRoleSkillIfMissing("Software Engineer", skills.get("sql"), "INTERMEDIATE", 0.80, true);
        insertRoleSkillIfMissing("Software Engineer", skills.get("rest apis"), "INTERMEDIATE", 0.85, true);
        insertRoleSkillIfMissing("Software Engineer", skills.get("git"), "INTERMEDIATE", 0.75, true);
        insertRoleSkillIfMissing("Software Engineer", skills.get("unit testing"), "INTERMEDIATE", 0.80, true);
        insertRoleSkillIfMissing("Software Engineer", skills.get("system design"), "BEGINNER", 0.70, false);

        insertRoleSkillIfMissing("Data Analyst", skills.get("sql"), "ADVANCED", 1.00, true);
        insertRoleSkillIfMissing("Data Analyst", skills.get("python"), "INTERMEDIATE", 0.90, true);
        insertRoleSkillIfMissing("Data Analyst", skills.get("pandas"), "ADVANCED", 0.95, true);
        insertRoleSkillIfMissing("Data Analyst", skills.get("data visualization"), "ADVANCED", 0.90, true);
        insertRoleSkillIfMissing("Data Analyst", skills.get("git"), "BEGINNER", 0.60, false);

        insertRoleSkillIfMissing("AI/ML Engineer", skills.get("python"), "ADVANCED", 1.00, true);
        insertRoleSkillIfMissing("AI/ML Engineer", skills.get("machine learning"), "ADVANCED", 1.00, true);
        insertRoleSkillIfMissing("AI/ML Engineer", skills.get("deep learning"), "INTERMEDIATE", 0.90, true);
        insertRoleSkillIfMissing("AI/ML Engineer", skills.get("pandas"), "INTERMEDIATE", 0.85, true);
        insertRoleSkillIfMissing("AI/ML Engineer", skills.get("data structures"), "INTERMEDIATE", 0.80, true);
        insertRoleSkillIfMissing("AI/ML Engineer", skills.get("docker"), "BEGINNER", 0.65, false);
    }

    private void insertRoleSkillIfMissing(String roleName, Skill skill, String minProf, double weight, boolean core) {
        if (skill == null) return;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM target_role_skills WHERE role_name = ? AND skill_id = ?",
                Integer.class, roleName, skill.getId());
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO target_role_skills (role_name, skill_id, minimum_proficiency, importance_weight, is_core) VALUES (?, ?, ?, ?, ?)",
                    roleName, skill.getId(), minProf, weight, core);
        }
    }

    private Map<String, Opportunity> ensureOpportunities(Map<String, Skill> skills) {
        Map<String, Opportunity> existingMap = new HashMap<>();
        for (Opportunity existing : opportunityRepository.findAll()) {
            existingMap.put(existing.getTitle().toLowerCase(), existing);
        }

        List<Opportunity> catalog = buildSeedCatalog();
        Map<String, List<SkillLink>> skillLinks = buildOpportunitySkillLinks(skills);

        for (Opportunity opp : catalog) {
            String key = opp.getTitle().toLowerCase();
            if (!existingMap.containsKey(key)) {
                Opportunity saved = opportunityRepository.save(opp);
                for (SkillLink link : skillLinks.getOrDefault(opp.getTitle(), List.of())) {
                    if (link.skill() != null) {
                        opportunityRepository.attachSkills(saved.getId(), link.skill().getId(), link.rel(), link.prof());
                    }
                }
                opportunityRepository.findById(saved.getId()).ifPresent(full -> existingMap.put(key, full));
            }
        }
        return existingMap;
    }

    private void ensureInitialDemoStudents(Map<String, Skill> skills, Map<String, Opportunity> opportunities) {
        Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        if (studentCount != null && studentCount > 0) {
            return;
        }

        String defaultHash = PasswordHasher.hash(demoPassword);

        StudentProfile alex = new StudentProfile(null, "Alex Rivera", "alex.rivera@university.edu",
                "Java Backend Developer", 18, BigDecimal.valueOf(150.00), DifficultyLevel.INTERMEDIATE,
                "Junior CS student passionate about distributed systems, backend concurrency, and clean enterprise software.");
        alex.setPasswordHash(defaultHash);
        alex.setDemoAccount(true);
        alex.setSkills(List.of(
                new StudentSkill(null, skills.get("java"), ProficiencyLevel.INTERMEDIATE, 1.5, true),
                new StudentSkill(null, skills.get("sql"), ProficiencyLevel.BEGINNER, 0.5, false),
                new StudentSkill(null, skills.get("git"), ProficiencyLevel.INTERMEDIATE, 1.0, true)
        ));
        alex = studentRepository.save(alex);

        StudentProfile priya = new StudentProfile(null, "Priya Sharma", "priya.sharma@tech.edu",
                "AI/ML Engineer", 20, BigDecimal.valueOf(200.00), DifficultyLevel.ADVANCED,
                "Final-year engineering student focusing on neural models, deep computer vision, and applied ML pipelines.");
        priya.setPasswordHash(defaultHash);
        priya.setDemoAccount(true);
        priya.setSkills(List.of(
                new StudentSkill(null, skills.get("python"), ProficiencyLevel.ADVANCED, 2.0, true),
                new StudentSkill(null, skills.get("machine learning"), ProficiencyLevel.INTERMEDIATE, 1.0, true),
                new StudentSkill(null, skills.get("data structures"), ProficiencyLevel.INTERMEDIATE, 1.5, false)
        ));
        studentRepository.save(priya);

        StudentProfile marcus = new StudentProfile(null, "Marcus Chen", "marcus.chen@college.edu",
                "Data Analyst", 12, BigDecimal.valueOf(50.00), DifficultyLevel.BEGINNER,
                "Economics and informatics student keen on relational databases, visual storytelling, and business analytics.");
        marcus.setPasswordHash(defaultHash);
        marcus.setDemoAccount(true);
        marcus.setSkills(List.of(
                new StudentSkill(null, skills.get("sql"), ProficiencyLevel.BEGINNER, 0.3, false),
                new StudentSkill(null, skills.get("python"), ProficiencyLevel.BEGINNER, 0.5, false)
        ));
        studentRepository.save(marcus);

        Opportunity javaCourse = opportunities.get("modern java 21 & concurrency masterclass");
        Opportunity orderProj = opportunities.get("distributed order processing system");
        Opportunity springCourse = opportunities.get("spring boot 3 enterprise microservices");

        if (javaCourse != null) {
            feedbackRepository.save(new UserFeedback(null, alex.getId(), javaCourse.getId(),
                    FeedbackAction.SAVED, null, "Strong concurrency curriculum covering virtual threads."));
            learningPlanRepository.save(new LearningPlanItem(null, alex.getId(), javaCourse.getId(), 1,
                    PlanStatus.IN_PROGRESS, 6, "Builds strong Java 21 fundamentals for backend role"));
        }
        if (orderProj != null) {
            feedbackRepository.save(new UserFeedback(null, alex.getId(), orderProj.getId(),
                    FeedbackAction.RATED, 5, "Great practical Spring Boot & JDBC transaction management project."));
            learningPlanRepository.save(new LearningPlanItem(null, alex.getId(), orderProj.getId(), 2,
                    PlanStatus.PENDING, 4, "Hands-on Spring Boot & JDBC order processing"));
        }
        if (springCourse != null) {
            learningPlanRepository.save(new LearningPlanItem(null, alex.getId(), springCourse.getId(), 3,
                    PlanStatus.PENDING, 8, "Advanced enterprise architecture and microservices"));
        }

        log.info("Initialized default catalog and 3 demo student profiles cleanly.");
    }

    private List<Opportunity> buildSeedCatalog() {
        List<Opportunity> list = new ArrayList<>();

        list.add(createCourse("Modern Java 21 & Concurrency Masterclass",
                "Deep dive into Java 21 virtual threads, structured concurrency, generics, and high-performance collections.",
                DifficultyLevel.INTERMEDIATE, 45.0, 39.99, "JavaAcademy", "https://javaacademy.dev/courses/java21",
                4.85, 230, "JavaAcademy",
                "Module 1: Java 21 Syntax & Records. Module 2: Virtual Threads & Concurrency. Module 3: Advanced Generics & Collections. Module 4: Memory Optimization.",
                true, 6));

        list.add(createCourse("Spring Boot 3 Enterprise Microservices",
                "Architect production-grade enterprise microservices with Spring Boot 3, Spring Data JDBC, Security, and Docker.",
                DifficultyLevel.ADVANCED, 60.0, 59.99, "CloudEnterprise Labs", "https://cloudlabs.dev/spring-boot-3",
                4.90, 412, "CloudEnterprise Labs",
                "Module 1: Spring Boot Architecture. Module 2: REST APIs & JDBC. Module 3: Security & OAuth2. Module 4: Docker & CI/CD Pipelines.",
                true, 8));

        list.add(createCourse("SQL Performance Tuning & Relational Modeling",
                "Master query execution plans, indexing strategies, ACID transactions, and high-throughput schema design.",
                DifficultyLevel.INTERMEDIATE, 30.0, 29.99, "DataCraft", "https://datacraft.org/sql-tuning",
                4.75, 180, "DataCraft",
                "Module 1: Relational Algebra & SQL. Module 2: B-Tree Indexing. Module 3: Query Optimization Plans. Module 4: Transactions & Isolation Levels.",
                true, 4));

        list.add(createCourse("Applied Data Analysis with Python & Pandas",
                "Hands-on exploration of real-world datasets, statistical summarization, data wrangling, and interactive dashboards.",
                DifficultyLevel.BEGINNER, 35.0, 19.99, "DataCampus", "https://datacampus.org/python-pandas",
                4.70, 520, "DataCampus",
                "Module 1: Python Fundamentals. Module 2: Pandas Series & DataFrames. Module 3: Data Cleaning & Imputation. Module 4: Exploratory Analysis.",
                true, 5));

        list.add(createCourse("Deep Learning & Neural Architectures",
                "Comprehensive foundation covering CNNs, RNNs, Transformers, backpropagation calculus, and PyTorch deployment.",
                DifficultyLevel.ADVANCED, 70.0, 79.99, "AI University", "https://aiuniv.org/deep-learning",
                4.88, 310, "AI University",
                "Module 1: Deep Feedforward Networks. Module 2: Convolutional Networks. Module 3: Transformers & Attention. Module 4: Model Serving & Quantization.",
                true, 10));

        list.add(createProject("High-Throughput Concurrent Cache Engine",
                "Build a production-ready, lock-striped LRU memory cache in Java with generic key-value store, expiration, and benchmarks.",
                DifficultyLevel.ADVANCED, 25.0, "GitHub OpenSource Incubator", "https://github.com/incubator/concurrent-cache",
                4.92, 95, "https://github.com/incubator/concurrent-cache", List.of("Java", "Multithreading", "JUnit 5", "JMH"),
                ProjectTier.CAPSTONE, "A thread-safe generic cache library with eviction policies, concurrency benchmarks, and comprehensive unit tests."));

        list.add(createProject("Distributed Order Processing System",
                "Implement an event-driven inventory and order processing system using Spring Boot, JDBC, and transaction management.",
                DifficultyLevel.INTERMEDIATE, 40.0, "OpenDev Collective", "https://github.com/opendev/order-processor",
                4.80, 145, "https://github.com/opendev/order-processor", List.of("Java", "Spring Boot", "JDBC", "Docker", "MySQL"),
                ProjectTier.GUIDED, "A complete backend service processing orders, managing stock locks, and recording transactions with rollback handling."));

        list.add(createProject("Real-Time Financial Dashboard with Pandas",
                "Build a live financial portfolio analytics dashboard computing risk metrics, Sharpe ratio, and trend charts.",
                DifficultyLevel.INTERMEDIATE, 20.0, "FinData Hub", "https://github.com/findata/portfolio-analyzer",
                4.65, 88, "https://github.com/findata/portfolio-analyzer", List.of("Python", "Pandas", "Data Visualization", "REST APIs"),
                ProjectTier.GUIDED, "A clean Python dashboard reading market quotes, computing rolling variance, and rendering interactive performance plots."));

        list.add(createProject("Chest X-Ray Pathology Classifier",
                "Train an explainable vision transformer model with Grad-CAM visual heatmaps for diagnostic radiograph interpretation.",
                DifficultyLevel.ADVANCED, 50.0, "HealthAI Labs", "https://github.com/healthai/xray-classifier",
                4.91, 112, "https://github.com/healthai/xray-classifier", List.of("Python", "Deep Learning", "Machine Learning", "Docker"),
                ProjectTier.OPEN_SOURCE, "A trained PyTorch deep convolutional pipeline with attention maps showing diagnostic explainability for radiologist review."));

        list.add(createInternship("Backend Systems Engineering Intern",
                "Join an agile engineering squad building resilient Java microservices, API integrations, and database optimizations.",
                DifficultyLevel.INTERMEDIATE, 160.0, "FinTech Cloud Technologies", "https://fintechcloud.com/careers/intern-backend",
                4.85, 34, "FinTech Cloud Technologies", 1800.00, LocationType.REMOTE, 3, "2026-12-15"));

        list.add(createInternship("Junior Data & Analytics Intern",
                "Collaborate with product analytics team to write SQL extraction queries, build automated KPI dashboards, and clean data.",
                DifficultyLevel.BEGINNER, 120.0, "Global Logistics Corp", "https://globallogistics.com/interns",
                4.60, 21, "Global Logistics Corp", 1200.00, LocationType.HYBRID, 3, "2026-11-30"));

        list.add(createInternship("Applied AI Research Intern",
                "Develop and benchmark computer vision and NLP models on proprietary datasets in a hybrid research environment.",
                DifficultyLevel.ADVANCED, 200.0, "Nexus AI Systems", "https://nexusai.io/careers/research-intern",
                4.94, 18, "Nexus AI Systems", 2400.00, LocationType.REMOTE, 4, "2026-12-01"));

        list.add(createCert("Oracle Certified Professional: Java SE 21 Developer",
                "Globally recognized gold standard certification verifying deep mastery of Java language features, JVM internals, and APIs.",
                DifficultyLevel.ADVANCED, 120.0, 245.00, "Oracle University", "https://oracle.com/certification/java-se-21",
                4.95, 680, "Oracle University", "1Z0-830", 5,
                "Proctored Multiple Choice & Scenario Questions (50 questions, 120 min)"));

        list.add(createCert("AWS Certified Solutions Architect - Associate",
                "Validates expertise in designing resilient, cost-effective, high-performing cloud architectures on Amazon Web Services.",
                DifficultyLevel.INTERMEDIATE, 80.0, 150.00, "Amazon Web Services", "https://aws.amazon.com/certification/solutions-architect",
                4.88, 1250, "Amazon Web Services", "SAA-C03", 3,
                "Proctored Multiple Choice & Multi-Response (65 questions, 130 min)"));

        return list;
    }

    private CourseOpportunity createCourse(String title, String desc, DifficultyLevel diff, double hours, double cost,
                                           String provider, String url, double rating, int ratingCount,
                                           String platform, String syllabus, boolean cert, int weeks) {
        CourseOpportunity c = new CourseOpportunity(null, title, desc, diff, hours, BigDecimal.valueOf(cost),
                provider, url, platform, syllabus, cert, weeks);
        c.setRating(rating);
        c.setRatingCount(ratingCount);
        return c;
    }

    private ProjectOpportunity createProject(String title, String desc, DifficultyLevel diff, double hours,
                                             String provider, String url, double rating, int ratingCount,
                                             String repoUrl, List<String> stack, ProjectTier tier, String deliverable) {
        ProjectOpportunity p = new ProjectOpportunity(null, title, desc, diff, hours, BigDecimal.ZERO,
                provider, url, repoUrl, stack, tier, deliverable);
        p.setRating(rating);
        p.setRatingCount(ratingCount);
        return p;
    }

    private InternshipOpportunity createInternship(String title, String desc, DifficultyLevel diff, double hours,
                                                   String provider, String url, double rating, int ratingCount,
                                                   String company, double stipend, LocationType loc, int months, String deadline) {
        InternshipOpportunity i = new InternshipOpportunity(null, title, desc, diff, hours, BigDecimal.ZERO,
                provider, url, company, BigDecimal.valueOf(stipend), loc, months, deadline);
        i.setRating(rating);
        i.setRatingCount(ratingCount);
        return i;
    }

    private CertificationOpportunity createCert(String title, String desc, DifficultyLevel diff, double hours, double cost,
                                                String provider, String url, double rating, int ratingCount,
                                                String org, String examCode, int validityYears, String format) {
        CertificationOpportunity cert = new CertificationOpportunity(null, title, desc, diff, hours,
                BigDecimal.valueOf(cost), provider, url, org, examCode, validityYears, format);
        cert.setRating(rating);
        cert.setRatingCount(ratingCount);
        return cert;
    }

    private Map<String, List<SkillLink>> buildOpportunitySkillLinks(Map<String, Skill> skills) {
        Map<String, List<SkillLink>> map = new HashMap<>();

        map.put("Modern Java 21 & Concurrency Masterclass", List.of(
                new SkillLink(skills.get("java"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("java"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("multithreading"), "GAINED", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("data structures"), "GAINED", ProficiencyLevel.INTERMEDIATE)
        ));
        map.put("Spring Boot 3 Enterprise Microservices", List.of(
                new SkillLink(skills.get("java"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("sql"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("spring boot"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("jdbc"), "GAINED", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("rest apis"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("docker"), "GAINED", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("microservices"), "GAINED", ProficiencyLevel.INTERMEDIATE)
        ));
        map.put("SQL Performance Tuning & Relational Modeling", List.of(
                new SkillLink(skills.get("sql"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("sql"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("Applied Data Analysis with Python & Pandas", List.of(
                new SkillLink(skills.get("python"), "GAINED", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("pandas"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("data visualization"), "GAINED", ProficiencyLevel.INTERMEDIATE)
        ));
        map.put("Deep Learning & Neural Architectures", List.of(
                new SkillLink(skills.get("python"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("machine learning"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("deep learning"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("High-Throughput Concurrent Cache Engine", List.of(
                new SkillLink(skills.get("java"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("multithreading"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("multithreading"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("unit testing"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("data structures"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("Distributed Order Processing System", List.of(
                new SkillLink(skills.get("java"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("spring boot"), "GAINED", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("jdbc"), "GAINED", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("rest apis"), "GAINED", ProficiencyLevel.INTERMEDIATE)
        ));
        map.put("Real-Time Financial Dashboard with Pandas", List.of(
                new SkillLink(skills.get("python"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("pandas"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("data visualization"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("Chest X-Ray Pathology Classifier", List.of(
                new SkillLink(skills.get("python"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("machine learning"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("deep learning"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("unit testing"), "GAINED", ProficiencyLevel.INTERMEDIATE)
        ));
        map.put("Backend Systems Engineering Intern", List.of(
                new SkillLink(skills.get("java"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("sql"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("rest apis"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("microservices"), "GAINED", ProficiencyLevel.INTERMEDIATE)
        ));
        map.put("Junior Data & Analytics Intern", List.of(
                new SkillLink(skills.get("sql"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("python"), "PREREQUISITE", ProficiencyLevel.BEGINNER),
                new SkillLink(skills.get("pandas"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("data visualization"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("Applied AI Research Intern", List.of(
                new SkillLink(skills.get("python"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("machine learning"), "PREREQUISITE", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("deep learning"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("Oracle Certified Professional: Java SE 21 Developer", List.of(
                new SkillLink(skills.get("java"), "PREREQUISITE", ProficiencyLevel.INTERMEDIATE),
                new SkillLink(skills.get("java"), "GAINED", ProficiencyLevel.EXPERT),
                new SkillLink(skills.get("multithreading"), "GAINED", ProficiencyLevel.ADVANCED)
        ));
        map.put("AWS Certified Solutions Architect - Associate", List.of(
                new SkillLink(skills.get("system design"), "GAINED", ProficiencyLevel.ADVANCED),
                new SkillLink(skills.get("docker"), "GAINED", ProficiencyLevel.ADVANCED)
        ));

        return map;
    }

    private record SkillLink(Skill skill, String rel, ProficiencyLevel prof) {}
}
