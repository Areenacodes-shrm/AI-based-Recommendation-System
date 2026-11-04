package com.pathpilot.service.impl;

import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.repository.LearningPlanRepository;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.RoadmapService;
import com.pathpilot.service.SkillGapService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final SkillGapService skillGapService;

    public RoadmapServiceImpl(StudentRepository studentRepository,
                              OpportunityRepository opportunityRepository,
                              LearningPlanRepository learningPlanRepository,
                              SkillGapService skillGapService) {
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.skillGapService = skillGapService;
    }

    @Override
    @Transactional
    public List<LearningPlanItem> generateRoadmap(Long studentId) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // Clear previous plan if regenerating
        learningPlanRepository.clearForStudent(studentId);

        // Analyze skill gaps to determine priority learning objectives
        SkillGapAnalysisDto gapAnalysis = skillGapService.analyzeSkillGap(studentId, student.getTargetRole());

        Set<String> targetGapSkills = new HashSet<>();
        for (SkillGapAnalysisDto.SkillItemDto m : gapAnalysis.getMissingSkills()) {
            targetGapSkills.add(m.getSkillName().toLowerCase());
        }
        for (SkillGapAnalysisDto.SkillItemDto w : gapAnalysis.getWeakSkills()) {
            targetGapSkills.add(w.getSkillName().toLowerCase());
        }

        // Find opportunities that teach these skills
        List<Opportunity> allOpportunities = opportunityRepository.findAll();
        List<Opportunity> candidates = new ArrayList<>();

        for (Opportunity opp : allOpportunities) {
            boolean teachesGap = opp.getSkillsGained().stream()
                    .anyMatch(s -> targetGapSkills.contains(s.getName().toLowerCase()));
            if (teachesGap) {
                candidates.add(opp);
            }
        }

        // Sort candidates pedagogically:
        // 1. Difficulty Level (Beginner -> Intermediate -> Advanced -> Expert)
        // 2. Type: Course (foundational) -> Project (application) -> Certification / Internship (credentialing)
        candidates.sort((a, b) -> {
            int diffCmp = Integer.compare(
                    a.getDifficultyLevel().getNumericRank(),
                    b.getDifficultyLevel().getNumericRank()
            );
            if (diffCmp != 0) return diffCmp;

            // Prioritize Courses before Projects and Internships
            int typeWeightA = getTypeOrderWeight(a.getOpportunityType());
            int typeWeightB = getTypeOrderWeight(b.getOpportunityType());
            return Integer.compare(typeWeightA, typeWeightB);
        });

        // Take top 4 distinct roadmap milestones
        List<Opportunity> selected = candidates.stream().limit(4).toList();

        List<LearningPlanItem> generatedItems = new ArrayList<>();
        int sequence = 1;
        int weeklyHours = Math.max(5, student.getWeeklyHoursAvailable());

        for (Opportunity opp : selected) {
            // Target completion weeks based on estimated effort / weekly capacity
            int calculatedWeeks = (int) Math.ceil(opp.getEstimatedHours() / weeklyHours);
            int targetWeeks = Math.max(2, Math.min(12, calculatedWeeks));

            String note = "Step " + sequence + ": Advance competencies in " + student.getTargetRole();
            if (!opp.getSkillsGained().isEmpty()) {
                note = "Teaches: " + opp.getSkillsGained().get(0).getName();
            }

            PlanStatus initialStatus = (sequence == 1) ? PlanStatus.IN_PROGRESS : PlanStatus.PENDING;

            LearningPlanItem item = new LearningPlanItem(
                    null, studentId, opp.getId(), sequence, initialStatus, targetWeeks, note
            );
            LearningPlanItem saved = learningPlanRepository.save(item);
            saved.setOpportunity(opp);
            generatedItems.add(saved);
            sequence++;
        }

        return generatedItems;
    }

    private int getTypeOrderWeight(OpportunityType type) {
        return switch (type) {
            case COURSE -> 1;
            case PROJECT -> 2;
            case CERTIFICATION -> 3;
            case INTERNSHIP -> 4;
        };
    }

    @Override
    public List<LearningPlanItem> getRoadmap(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        List<LearningPlanItem> items = learningPlanRepository.findByStudentId(studentId);
        if (items.isEmpty()) {
            return generateRoadmap(studentId);
        }
        return items;
    }

    @Override
    @Transactional
    public LearningPlanItem updateItemStatus(Long planItemId, PlanStatus status, Integer targetWeeks) {
        LearningPlanItem item = learningPlanRepository.findById(planItemId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningPlanItem", planItemId));

        learningPlanRepository.updateStatus(planItemId, status, targetWeeks);
        return learningPlanRepository.findById(planItemId).orElse(item);
    }

    @Override
    public boolean deleteRoadmapItem(Long planItemId) {
        return learningPlanRepository.deleteById(planItemId);
    }
}
