package com.pathpilot.recommendation;

import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.CourseOpportunity;
import com.pathpilot.model.opportunity.ProjectOpportunity;
import com.pathpilot.recommendation.rerank.DiversityReranker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MMR Diversity Reranker Unit Tests")
class DiversityRerankerTest {

    private final DiversityReranker reranker = new DiversityReranker();

    @Test
    @DisplayName("Should keep highest scoring item first while promoting diverse types and non-overlapping skills")
    void shouldPromoteDiverseOpportunityOverRedundantDuplicate() {
        Skill java = new Skill(1L, "Java", "LANG", "Backend", "");
        Skill spring = new Skill(2L, "Spring Boot", "FW", "Backend", "");
        Skill docker = new Skill(6L, "Docker", "DEVOPS", "Infra", "");
        Skill testing = new Skill(15L, "Unit Testing", "METH", "SWE", "");

        CourseOpportunity courseA = new CourseOpportunity(
                1L, "Spring Boot Deep Dive A", "Course A", DifficultyLevel.INTERMEDIATE,
                40.0, BigDecimal.valueOf(30), "Udemy", "url", "Udemy", "Syllabus", true, 5);
        courseA.setSkillsGained(List.of(java, spring));

        CourseOpportunity courseB = new CourseOpportunity(
                2L, "Spring Boot Deep Dive B", "Course B", DifficultyLevel.INTERMEDIATE,
                42.0, BigDecimal.valueOf(35), "Coursera", "url", "Coursera", "Syllabus", true, 5);
        courseB.setSkillsGained(List.of(java, spring));

        ProjectOpportunity capstone = new ProjectOpportunity(
                3L, "Containerized Microservice Capstone", "Project", DifficultyLevel.INTERMEDIATE,
                25.0, BigDecimal.ZERO, "GitHub", "url", "repo", List.of("Docker", "JUnit"), ProjectTier.GUIDED, "Repo");
        capstone.setSkillsGained(List.of(docker, testing));

        RecommendationItem itemA = new RecommendationItem(courseA, 0.90, 0.9, 0.9, 0.9, 0.9, new ExplanationData());
        RecommendationItem itemB = new RecommendationItem(courseB, 0.88, 0.88, 0.88, 0.88, 0.88, new ExplanationData());
        RecommendationItem itemC = new RecommendationItem(capstone, 0.84, 0.84, 0.84, 0.84, 0.84, new ExplanationData());

        List<RecommendationItem> reranked = reranker.rerank(List.of(itemA, itemB, itemC), 3);

        assertEquals(3, reranked.size());
        assertEquals(1L, reranked.get(0).getOpportunity().getId(), "Highest relevance item must remain Rank #1");
        assertEquals(3L, reranked.get(1).getOpportunity().getId(), "Diverse non-overlapping Project should beat duplicate Course");
        assertEquals(2L, reranked.get(2).getOpportunity().getId());

        assertEquals(1, reranked.get(0).getRankOrder());
        assertEquals(2, reranked.get(1).getRankOrder());
        assertEquals(3, reranked.get(2).getRankOrder());
        assertEquals(0.84, reranked.get(1).getRankingScore(), "Reranking must not falsify underlying relevance scores");
    }

    @Test
    @DisplayName("Should handle empty and single-item inputs safely")
    void shouldHandleEdgeCases() {
        assertTrue(reranker.rerank(List.of(), 5).isEmpty());

        CourseOpportunity single = new CourseOpportunity(
                10L, "Solo Course", "Desc", DifficultyLevel.BEGINNER,
                10.0, BigDecimal.ZERO, "Prov", "url", "Plat", "Syl", false, 2);
        RecommendationItem item = new RecommendationItem(single, 0.75, 0.7, 0.8, 0.7, 0.7, new ExplanationData());

        List<RecommendationItem> result = reranker.rerank(List.of(item), 3);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRankOrder());
    }
}
