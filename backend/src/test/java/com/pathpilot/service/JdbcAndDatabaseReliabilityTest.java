package com.pathpilot.service;

import com.pathpilot.config.SeedDataInitializer;
import com.pathpilot.dto.WeeklyProgressSummaryDto;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.CourseOpportunity;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.LearningPlanRepository;
import com.pathpilot.repository.OpportunityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Database Reliability, Idempotency & Roadmap Export Integration Tests")
class JdbcAndDatabaseReliabilityTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SeedDataInitializer seedDataInitializer;

    @Autowired
    private StudentService studentService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private LearningPlanRepository learningPlanRepository;

    @Autowired
    private RoadmapService roadmapService;

    @Test
    @DisplayName("Re-running schema.sql and SeedDataInitializer must preserve existing data without duplicates")
    void shouldPreserveUserDataAcrossSchemaAndSeedReruns() {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        jdbcTemplate.update("UPDATE students SET bio = ? WHERE id = ?", "Custom edited bio preserved", alex.getId());

        int beforeOpps = opportunityRepository.findAll().size();
        int beforeStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);
        seedDataInitializer.seedAll();

        int afterOpps = opportunityRepository.findAll().size();
        int afterStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        StudentProfile reloaded = studentService.getStudentById(alex.getId());

        assertEquals(beforeOpps, afterOpps);
        assertEquals(beforeStudents, afterStudents);
        assertEquals("Custom edited bio preserved", reloaded.getBio());
    }

    @Test
    @DisplayName("Should export roadmap CSV safely with formula escaping and handle empty roadmaps gracefully")
    void shouldExportSafeCsvForPopulatedAndEmptyRoadmaps() {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        CourseOpportunity trickyOpp = new CourseOpportunity(
                null, "=HYPERLINK(\"http://bad.example\",\"Click\")", "Formula test description",
                DifficultyLevel.BEGINNER, 12.0, BigDecimal.ZERO, "TestOrg", "https://example.org",
                "TestOrg", "Syl", false, 2
        );
        trickyOpp = (CourseOpportunity) opportunityRepository.save(trickyOpp);

        LearningPlanItem added = roadmapService.addOpportunityToRoadmap(alex.getId(), trickyOpp.getId(), "+SUM(A1:A2)");
        String csv = roadmapService.exportRoadmapCsv(alex.getId());

        assertTrue(csv.startsWith("Sequence,Opportunity Title,Opportunity Type"));
        assertTrue(csv.contains("\"'=HYPERLINK("), "Formula prefix must be neutralized cleanly in CSV cells");
        assertTrue(csv.contains("\"'+SUM(A1:A2)\""), "Plus prefix must be neutralized cleanly in CSV notes");

        // Idempotent re-add of the same opportunity should return existing milestone rather than duplicating
        LearningPlanItem duplicateCheck = roadmapService.addOpportunityToRoadmap(alex.getId(), trickyOpp.getId(), "Second attempt");
        assertEquals(added.getId(), duplicateCheck.getId());

        // Clearing saved items must keep CSV export valid with header-only (no unintended auto-regeneration)
        learningPlanRepository.clearForStudent(alex.getId());
        String emptyCsv = roadmapService.exportRoadmapCsv(alex.getId());
        String[] lines = emptyCsv.trim().split("\\r?\\n");
        assertEquals(1, lines.length);
    }

    @Test
    @DisplayName("Parameterized updateStatus and weekly progress summary should compute realistic metrics")
    void shouldComputeWeeklyProgressAndUpdateStatusSafely() {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        List<LearningPlanItem> items = roadmapService.getRoadmap(alex.getId());
        assertFalse(items.isEmpty());

        LearningPlanItem first = items.get(0);
        LearningPlanItem updated = roadmapService.updateItemStatus(first.getId(), PlanStatus.COMPLETED, 5);
        assertEquals(PlanStatus.COMPLETED, updated.getStatus());
        assertEquals(5, updated.getTargetCompletionWeeks());

        WeeklyProgressSummaryDto summary = roadmapService.getWeeklyProgressSummary(alex.getId());
        assertEquals(alex.getId(), summary.getStudentId());
        assertEquals(items.size(), summary.getTotalItems());
        assertTrue(summary.getCompletedItems() >= 1);
        assertTrue(summary.getCompletedHours() > 0.0);
        assertTrue(summary.getProjectedRoleMatchPercentage() >= summary.getCurrentRoleMatchPercentage());
    }
}
