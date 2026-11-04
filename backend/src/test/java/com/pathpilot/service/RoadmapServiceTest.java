package com.pathpilot.service;

import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.CourseOpportunity;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.LearningPlanRepository;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.impl.RoadmapServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Learning Roadmap Generation Service Test")
class RoadmapServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private LearningPlanRepository learningPlanRepository;

    @Mock
    private SkillGapService skillGapService;

    @InjectMocks
    private RoadmapServiceImpl roadmapService;

    @Test
    @DisplayName("Should generate pedagogical roadmap addressing missing skills")
    void shouldGeneratePedagogicalRoadmap() {
        StudentProfile student = new StudentProfile(
                1L, "Alex Rivera", "alex@test.com", "Java Backend Developer",
                15, BigDecimal.valueOf(100), DifficultyLevel.INTERMEDIATE, "Bio"
        );

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        SkillGapAnalysisDto gapAnalysis = new SkillGapAnalysisDto();
        gapAnalysis.setTargetRole("Java Backend Developer");
        gapAnalysis.setMissingSkills(List.of(
                new SkillGapAnalysisDto.SkillItemDto("Spring Boot", "FRAMEWORK", null, ProficiencyLevel.ADVANCED, 0.9, true)
        ));
        when(skillGapService.analyzeSkillGap(1L, "Java Backend Developer")).thenReturn(gapAnalysis);

        Skill springBoot = new Skill(2L, "Spring Boot", "FRAMEWORK", "Backend", "Desc");
        CourseOpportunity course = new CourseOpportunity(
                2L, "Spring Boot Microservices", "Desc", DifficultyLevel.INTERMEDIATE,
                45.0, BigDecimal.valueOf(50), "Prov", "url", "Plat", "Syllabus", true, 6
        );
        course.setSkillsGained(List.of(springBoot));

        when(opportunityRepository.findAll()).thenReturn(List.of(course));
        when(learningPlanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<LearningPlanItem> roadmap = roadmapService.generateRoadmap(1L);

        assertNotNull(roadmap);
        assertEquals(1, roadmap.size());
        assertEquals(1, roadmap.get(0).getSequenceOrder());
        assertEquals(PlanStatus.IN_PROGRESS, roadmap.get(0).getStatus());
        verify(learningPlanRepository).clearForStudent(1L);
    }
}
