package com.pathpilot.service;

import com.pathpilot.dto.StudentRegistrationDto;
import com.pathpilot.dto.StudentSkillInputDto;
import com.pathpilot.dto.WhatIfSimulationRequestDto;
import com.pathpilot.dto.WhatIfSimulationResultDto;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.RecommendationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Career Simulator, Side-by-Side Comparison & Cold-Start Confidence Tests")
class CareerSimulatorAndComparisonTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Test
    @DisplayName("What-if career simulator should compute deltas without mutating persisted profile or history")
    void shouldSimulateWithoutPersistingChanges() {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        String originalRole = alex.getTargetRole();
        int originalHours = alex.getWeeklyHoursAvailable();
        BigDecimal originalBudget = alex.getMaxBudget();
        int originalSkillCount = alex.getSkillsList().size();

        recommendationService.getRecommendationsForStudent(alex.getId(), 5);
        List<RecommendationItem> beforeLatest = recommendationRepository.findLatestByStudentId(alex.getId());

        WhatIfSimulationRequestDto simReq = new WhatIfSimulationRequestDto();
        simReq.setTargetRole("AI/ML Engineer");
        simReq.setWeeklyHoursAvailable(25);
        simReq.setMaxBudget(BigDecimal.valueOf(300.00));
        simReq.setPreferredDifficulty(DifficultyLevel.ADVANCED);
        simReq.setSimulatedSkills(List.of(
                new StudentSkillInputDto(null, "Python", ProficiencyLevel.ADVANCED, 2.0),
                new StudentSkillInputDto(null, "Machine Learning", ProficiencyLevel.INTERMEDIATE, 1.0)
        ));
        simReq.setLimit(6);

        WhatIfSimulationResultDto simResult = recommendationService.simulateWhatIf(alex.getId(), simReq);

        assertFalse(simResult.isPersisted());
        assertEquals(originalRole, simResult.getBaselineTargetRole());
        assertEquals("AI/ML Engineer", simResult.getSimulatedTargetRole());
        assertFalse(simResult.getItems().isEmpty());

        // Verify persisted database profile and latest recommendation batch remain unchanged
        StudentProfile reloaded = studentService.getStudentById(alex.getId());
        assertEquals(originalRole, reloaded.getTargetRole());
        assertEquals(originalHours, reloaded.getWeeklyHoursAvailable());
        assertEquals(0, originalBudget.compareTo(reloaded.getMaxBudget()));
        assertEquals(originalSkillCount, reloaded.getSkillsList().size());

        List<RecommendationItem> afterLatest = recommendationRepository.findLatestByStudentId(alex.getId());
        assertEquals(beforeLatest.size(), afterLatest.size());
        if (!beforeLatest.isEmpty()) {
            assertEquals(beforeLatest.get(0).getOpportunity().getId(), afterLatest.get(0).getOpportunity().getId());
        }
    }

    @Test
    @DisplayName("Cold-start user without skills or feedback should report LIMITED_DATA_COLD_START honestly")
    void shouldReportColdStartAndFeedbackConfidenceBasis() {
        StudentRegistrationDto reg = new StudentRegistrationDto();
        reg.setFullName("Cold Start Learner");
        reg.setEmail("coldstart.test@university.edu");
        reg.setPassword("StrongPass!123");
        reg.setTargetRole("Data Analyst");
        reg.setWeeklyHoursAvailable(10);
        reg.setMaxBudget(BigDecimal.valueOf(50.00));
        reg.setPreferredDifficulty(DifficultyLevel.BEGINNER);

        StudentProfile coldStudent = studentService.registerStudent(reg);
        RecommendationResult coldRecs = recommendationService.getRecommendationsForStudent(coldStudent.getId(), 5);

        assertTrue(coldRecs.isColdStart());
        assertEquals(RelevanceBasis.LIMITED_DATA_COLD_START, coldRecs.getRelevanceBasis());
        assertFalse(coldRecs.getItems().isEmpty());
        ExplanationData firstExp = coldRecs.getItems().get(0).getExplanation();
        assertEquals(RelevanceBasis.LIMITED_DATA_COLD_START, firstExp.getRelevanceBasis());
        assertTrue(firstExp.getConfidenceLabel().contains("Cold-Start"));

        // Seeded Alex has skills + recorded feedback -> FEEDBACK_SUPPORTED
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        RecommendationResult alexRecs = recommendationService.getRecommendationsForStudent(alex.getId(), 5);
        assertFalse(alexRecs.isColdStart());
        assertEquals(RelevanceBasis.FEEDBACK_SUPPORTED, alexRecs.getRelevanceBasis());
    }

    @Test
    @DisplayName("Should compare 2-3 valid opportunities and reject invalid counts or missing IDs")
    void shouldCompareOpportunitiesWithValidation() {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        List<Opportunity> catalog = opportunityRepository.findAll();
        assertTrue(catalog.size() >= 4);

        List<RecommendationItem> compared = recommendationService.compareOpportunities(
                alex.getId(), List.of(catalog.get(0).getId(), catalog.get(1).getId(), catalog.get(2).getId())
        );
        assertEquals(3, compared.size());
        assertEquals(1, compared.get(0).getRankOrder());
        assertNotNull(compared.get(0).getExplanation());

        assertThrows(InvalidRecommendationRequestException.class,
                () -> recommendationService.compareOpportunities(alex.getId(), List.of(catalog.get(0).getId())));
        assertThrows(InvalidRecommendationRequestException.class,
                () -> recommendationService.compareOpportunities(alex.getId(), List.of(
                        catalog.get(0).getId(), catalog.get(1).getId(), catalog.get(2).getId(), catalog.get(3).getId()
                )));
        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService.compareOpportunities(alex.getId(), List.of(catalog.get(0).getId(), 999999L)));
    }
}
