package com.pathpilot.recommendation;

import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.CourseOpportunity;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.scoring.ContentBasedScorer;
import com.pathpilot.recommendation.scoring.ContextualScorer;
import com.pathpilot.recommendation.scoring.FeedbackScorer;
import com.pathpilot.recommendation.scoring.SkillMatchScorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Recommendation Scoring Strategies Unit Test")
class RecommendationScoringTest {

    private ContentBasedScorer contentScorer;
    private SkillMatchScorer skillMatchScorer;
    private ContextualScorer contextualScorer;
    private FeedbackScorer feedbackScorer;

    private StudentProfile student;
    private CourseOpportunity opportunity;
    private RecommendationContext context;

    @BeforeEach
    void setUp() {
        contentScorer = new ContentBasedScorer();
        skillMatchScorer = new SkillMatchScorer();
        contextualScorer = new ContextualScorer();
        feedbackScorer = new FeedbackScorer();

        student = new StudentProfile(
                1L, "Test Student", "test@domain.com", "Java Backend Developer",
                20, BigDecimal.valueOf(100.0), DifficultyLevel.INTERMEDIATE, "Focused on Java microservices and databases"
        );

        Skill javaSkill = new Skill(1L, "Java", "LANGUAGE", "Backend", "Core Java");
        student.addSkill(new StudentSkill(1L, javaSkill, ProficiencyLevel.INTERMEDIATE, 1.5, true));

        opportunity = new CourseOpportunity(
                10L, "Modern Java 21 & Concurrency Masterclass",
                "Learn Java 21 virtual threads, collections, and backend systems.",
                DifficultyLevel.INTERMEDIATE, 30.0, BigDecimal.valueOf(39.99),
                "JavaAcademy", "http://test", "JavaAcademy", "Syllabus", true, 6
        );

        Skill concurrencySkill = new Skill(5L, "Multithreading", "ARCHITECTURE", "Systems", "Concurrency");
        opportunity.setPrerequisiteSkills(List.of(javaSkill));
        opportunity.setSkillsGained(List.of(javaSkill, concurrencySkill));

        context = new RecommendationContext("Java Backend Developer");
        context.setRoleSkills(List.of(
                new TargetRoleSkill(1L, "Java Backend Developer", javaSkill, ProficiencyLevel.ADVANCED, 1.0, true),
                new TargetRoleSkill(2L, "Java Backend Developer", concurrencySkill, ProficiencyLevel.INTERMEDIATE, 0.9, true)
        ));
        context.setStudentSkills(student.getSkillsList());
    }

    @Test
    @DisplayName("Should normalize all individual scores between 0.0 and 1.0")
    void shouldNormalizeScoresBetweenZeroAndOne() {
        double content = contentScorer.score(student, opportunity, context);
        double skill = skillMatchScorer.score(student, opportunity, context);
        double contextual = contextualScorer.score(student, opportunity, context);
        double feedback = feedbackScorer.score(student, opportunity, context);

        assertTrue(content >= 0.0 && content <= 1.0, "Content score out of range: " + content);
        assertTrue(skill >= 0.0 && skill <= 1.0, "Skill score out of range: " + skill);
        assertTrue(contextual >= 0.0 && contextual <= 1.0, "Contextual score out of range: " + contextual);
        assertTrue(feedback >= 0.0 && feedback <= 1.0, "Feedback score out of range: " + feedback);

        // Content score should be positive because title and description contain 'Java' and 'backend'
        assertTrue(content >= 0.30, "Expected positive content match for Java student, was: " + content);
        // Prerequisite is satisfied, and it teaches missing Multithreading skill
        assertTrue(skill > 0.60, "Expected strong skill match for prerequisite-satisfied opportunity");
    }

    @Test
    @DisplayName("Should penalize opportunity that exceeds budget")
    void shouldPenalizeOverBudgetOpportunity() {
        opportunity.setCost(BigDecimal.valueOf(500.00)); // Way above student's $100 budget
        double contextual = contextualScorer.score(student, opportunity, context);

        opportunity.setCost(BigDecimal.ZERO); // Free
        double freeContextual = contextualScorer.score(student, opportunity, context);

        assertTrue(freeContextual > contextual, "Free opportunity should score higher than over-budget opportunity");
    }

    @Test
    @DisplayName("Should correctly sort recommendations using Comparator")
    void shouldSortRecommendationsUsingComparator() {
        RecommendationItem item1 = new RecommendationItem(opportunity, 0.85, 0.8, 0.9, 0.8, 0.9, null);
        RecommendationItem item2 = new RecommendationItem(opportunity, 0.92, 0.9, 0.95, 0.9, 0.9, null);
        RecommendationItem item3 = new RecommendationItem(opportunity, 0.65, 0.6, 0.7, 0.6, 0.7, null);

        List<RecommendationItem> list = new java.util.ArrayList<>(List.of(item1, item2, item3));
        list.sort(RecommendationItem.BY_RANKING_SCORE_DESC);

        assertEquals(0.92, list.get(0).getRankingScore());
        assertEquals(0.85, list.get(1).getRankingScore());
        assertEquals(0.65, list.get(2).getRankingScore());
    }
}
