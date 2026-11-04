package com.pathpilot.recommendation;

import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OOP Inheritance and Polymorphism Test")
class OpportunityPolymorphismTest {

    @Test
    @DisplayName("Should demonstrate polymorphic difficulty penalty dynamic dispatch")
    void shouldDemonstratePolymorphicDifficultyPenaltyDispatch() {
        StudentProfile beginnerStudent = new StudentProfile(
                1L, "Beginner Dev", "beginner@test.com", "Java Backend Developer",
                10, BigDecimal.valueOf(50), DifficultyLevel.BEGINNER, "Learning"
        );

        // All opportunities are ADVANCED level (delta = 3 - 1 = 2)
        Opportunity course = new CourseOpportunity(
                1L, "Adv Java", "Desc", DifficultyLevel.ADVANCED, 40.0, BigDecimal.valueOf(20),
                "Prov", "url", "Plat", "Syllabus", true, 6
        );

        Opportunity project = new ProjectOpportunity(
                2L, "Adv Project", "Desc", DifficultyLevel.ADVANCED, 40.0, BigDecimal.ZERO,
                "Prov", "url", "repo", List.of("Java"), ProjectTier.CAPSTONE, "Deliv"
        );

        Opportunity internship = new InternshipOpportunity(
                3L, "Adv Intern", "Desc", DifficultyLevel.ADVANCED, 160.0, BigDecimal.ZERO,
                "Prov", "url", "Google", BigDecimal.valueOf(2000), LocationType.REMOTE, 3, "2026-12"
        );

        Opportunity cert = new CertificationOpportunity(
                4L, "Adv Cert", "Desc", DifficultyLevel.ADVANCED, 100.0, BigDecimal.valueOf(200),
                "Prov", "url", "Oracle", "1Z0-830", 3, "Exam"
        );

        // Polymorphic dynamic method calls
        double coursePenalty = course.calculateDifficultyPenalty(beginnerStudent);
        double projectPenalty = project.calculateDifficultyPenalty(beginnerStudent);
        double internPenalty = internship.calculateDifficultyPenalty(beginnerStudent);
        double certPenalty = cert.calculateDifficultyPenalty(beginnerStudent);

        // Courses offer pedagogical scaffolding (penalty is lower)
        // Internships have strict hiring requirements (penalty is steepest)
        assertTrue(coursePenalty < projectPenalty, "Course penalty should be lower than project penalty");
        assertTrue(projectPenalty <= internPenalty, "Project penalty should be <= internship penalty");
        assertTrue(coursePenalty >= 0.0 && coursePenalty <= 1.0);
        assertTrue(internPenalty >= 0.0 && internPenalty <= 1.0);

        // Check polymorphic OpportunityType
        assertEquals(OpportunityType.COURSE, course.getOpportunityType());
        assertEquals(OpportunityType.PROJECT, project.getOpportunityType());
        assertEquals(OpportunityType.INTERNSHIP, internship.getOpportunityType());
        assertEquals(OpportunityType.CERTIFICATION, cert.getOpportunityType());
    }

    @Test
    @DisplayName("Should verify encapsulation and constraint checks")
    void shouldVerifyEncapsulationAndConstraints() {
        StudentProfile student = new StudentProfile();
        student.setMaxBudget(BigDecimal.valueOf(-50)); // Should normalize negative budget to 0
        assertEquals(BigDecimal.ZERO, student.getMaxBudget());

        student.setWeeklyHoursAvailable(120); // Should clamp to max 80
        assertEquals(80, student.getWeeklyHoursAvailable());
    }
}
