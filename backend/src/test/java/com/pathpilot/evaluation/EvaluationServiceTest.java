package com.pathpilot.evaluation;

import com.pathpilot.dto.EvaluationResultDto;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.CourseOpportunity;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.strategy.HybridRecommendationStrategy;
import com.pathpilot.recommendation.strategy.PopularityBaselineStrategy;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.SkillRepository;
import com.pathpilot.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Offline Recommendation Evaluation Test")
class EvaluationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private HybridRecommendationStrategy hybridStrategy;

    @Mock
    private PopularityBaselineStrategy baselineStrategy;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    @DisplayName("Should evaluate and benchmark Hybrid AI against Popularity Baseline")
    void shouldBenchmarkHybridAgainstPopularityBaseline() {
        StudentProfile student = new StudentProfile(
                1L, "Alex Rivera", "alex@test.com", "Java Backend Developer",
                15, BigDecimal.valueOf(100), DifficultyLevel.INTERMEDIATE, "Bio"
        );

        Skill java = new Skill(1L, "Java", "LANG", "Backend", "Desc");
        CourseOpportunity opp1 = new CourseOpportunity(
                1L, "Java Masterclass", "Desc", DifficultyLevel.INTERMEDIATE,
                30.0, BigDecimal.valueOf(30), "Prov", "url", "Plat", "Syllabus", true, 4
        );
        opp1.setSkillsGained(List.of(java));

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(opportunityRepository.findAll()).thenReturn(List.of(opp1));
        when(skillRepository.findRoleSkills("Java Backend Developer")).thenReturn(List.of(
                new TargetRoleSkill(1L, "Java Backend Developer", java, ProficiencyLevel.ADVANCED, 1.0, true)
        ));

        RecommendationItem hybridItem = new RecommendationItem(opp1, 0.9, 0.9, 0.9, 0.9, 0.9, null);
        when(hybridStrategy.recommend(any(), anyInt())).thenReturn(
                new RecommendationResult(1L, 1L, "Hybrid", List.of(hybridItem))
        );

        RecommendationItem baseItem = new RecommendationItem(opp1, 0.5, 0.5, 0.5, 0.5, 0.5, null);
        when(baselineStrategy.recommend(any(), anyInt())).thenReturn(
                new RecommendationResult(2L, 1L, "Baseline", List.of(baseItem))
        );

        EvaluationResultDto result = evaluationService.evaluate(1);

        assertNotNull(result);
        assertEquals(1, result.getTopK());
        assertEquals(1, result.getTotalTestUsers());
        assertEquals(1.0, result.getHybridPrecisionAtK());
        assertEquals(1.0, result.getHybridRecallAtK());
        assertEquals(1.0, result.getHybridNdcgAtK());
        assertEquals(100.0, result.getHybridCatalogCoverage());
    }
}
