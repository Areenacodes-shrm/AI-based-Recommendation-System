package com.pathpilot.service.impl;

import com.pathpilot.dto.SkillGapAnalysisDto;
import com.pathpilot.dto.WeeklyProgressSummaryDto;
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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

        learningPlanRepository.clearForStudent(studentId);

        SkillGapAnalysisDto gapAnalysis = skillGapService.analyzeSkillGap(studentId, student.getTargetRole());

        Set<String> targetGapSkills = new HashSet<>();
        for (SkillGapAnalysisDto.SkillItemDto m : gapAnalysis.getMissingSkills()) {
            targetGapSkills.add(m.getSkillName().toLowerCase());
        }
        for (SkillGapAnalysisDto.SkillItemDto w : gapAnalysis.getWeakSkills()) {
            targetGapSkills.add(w.getSkillName().toLowerCase());
        }

        List<Opportunity> allOpportunities = opportunityRepository.findAll();
        List<Opportunity> candidates = new ArrayList<>();

        for (Opportunity opp : allOpportunities) {
            boolean teachesGap = opp.getSkillsGained().stream()
                    .anyMatch(s -> targetGapSkills.contains(s.getName().toLowerCase()));
            if (teachesGap) {
                candidates.add(opp);
            }
        }

        candidates.sort((a, b) -> {
            int diffCmp = Integer.compare(
                    a.getDifficultyLevel().getNumericRank(),
                    b.getDifficultyLevel().getNumericRank()
            );
            if (diffCmp != 0) return diffCmp;

            int typeWeightA = getTypeOrderWeight(a.getOpportunityType());
            int typeWeightB = getTypeOrderWeight(b.getOpportunityType());
            return Integer.compare(typeWeightA, typeWeightB);
        });

        List<Opportunity> selected = candidates.stream().limit(4).toList();

        List<LearningPlanItem> generatedItems = new ArrayList<>();
        int sequence = 1;
        int weeklyHours = Math.max(5, student.getWeeklyHoursAvailable());

        for (Opportunity opp : selected) {
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
    public LearningPlanItem getItemById(Long planItemId) {
        return learningPlanRepository.findById(planItemId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningPlanItem", planItemId));
    }

    @Override
    @Transactional
    public LearningPlanItem addOpportunityToRoadmap(Long studentId, Long opportunityId, String note) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", opportunityId));

        Optional<LearningPlanItem> existing = learningPlanRepository.findByStudentIdAndOpportunityId(studentId, opportunityId);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<LearningPlanItem> currentPlan = learningPlanRepository.findByStudentId(studentId);
        int nextSeq = currentPlan.stream()
                .mapToInt(LearningPlanItem::getSequenceOrder)
                .max()
                .orElse(0) + 1;

        int weeklyHours = Math.max(5, student.getWeeklyHoursAvailable());
        int targetWeeks = Math.max(1, Math.min(16, (int) Math.ceil(opp.getEstimatedHours() / weeklyHours)));

        String effectiveNote = (note != null && !note.isBlank())
                ? note.trim()
                : (!opp.getSkillsGained().isEmpty()
                    ? "Builds: " + opp.getSkillsGained().stream().map(Skill::getName).limit(2).collect(Collectors.joining(", "))
                    : "Added " + opp.getOpportunityType().name().toLowerCase() + " milestone");

        LearningPlanItem newItem = new LearningPlanItem(
                null, studentId, opp.getId(), nextSeq, PlanStatus.PENDING, targetWeeks, effectiveNote
        );
        LearningPlanItem saved = learningPlanRepository.save(newItem);
        saved.setOpportunity(opp);
        return saved;
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
    @Transactional
    public boolean deleteRoadmapItem(Long planItemId) {
        learningPlanRepository.findById(planItemId)
                .orElseThrow(() -> new ResourceNotFoundException("LearningPlanItem", planItemId));
        return learningPlanRepository.deleteById(planItemId);
    }

    @Override
    public String exportRoadmapCsv(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        // Read persisted roadmap directly so an intentionally empty saved roadmap exports cleanly as header-only CSV
        List<LearningPlanItem> items = learningPlanRepository.findByStudentId(studentId);

        StringBuilder csv = new StringBuilder();
        csv.append("Sequence,Opportunity Title,Opportunity Type,Skills Targeted,Provider,Estimated Hours,Target Duration (Weeks),Status,Projected Target Date,Notes\n");

        LocalDate cursorDate = LocalDate.now();
        for (LearningPlanItem item : items) {
            Opportunity opp = item.getOpportunity();
            String title = opp != null ? opp.getTitle() : "Opportunity #" + item.getOpportunityId();
            String type = opp != null ? opp.getOpportunityType().name() : "";
            String provider = opp != null ? opp.getProvider() : "";
            String estHours = opp != null ? String.valueOf(opp.getEstimatedHours()) : "0";
            String skills = (opp != null && opp.getSkillsGained() != null)
                    ? opp.getSkillsGained().stream().map(Skill::getName).collect(Collectors.joining("; "))
                    : "";

            String targetDateStr;
            if (item.getStatus() == PlanStatus.COMPLETED) {
                targetDateStr = "Completed";
            } else if (item.getStatus() == PlanStatus.SKIPPED) {
                targetDateStr = "Skipped";
            } else {
                cursorDate = cursorDate.plusWeeks(Math.max(1, item.getTargetCompletionWeeks()));
                targetDateStr = cursorDate.toString();
            }

            csv.append(item.getSequenceOrder()).append(',')
               .append(escapeCsv(title)).append(',')
               .append(escapeCsv(type)).append(',')
               .append(escapeCsv(skills)).append(',')
               .append(escapeCsv(provider)).append(',')
               .append(escapeCsv(estHours)).append(',')
               .append(item.getTargetCompletionWeeks()).append(',')
               .append(escapeCsv(item.getStatus().name())).append(',')
               .append(escapeCsv(targetDateStr)).append(',')
               .append(escapeCsv(item.getPrerequisiteNote() != null ? item.getPrerequisiteNote() : ""))
               .append('\n');
        }
        return csv.toString();
    }

    @Override
    public WeeklyProgressSummaryDto getWeeklyProgressSummary(Long studentId) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        List<LearningPlanItem> items = learningPlanRepository.findByStudentId(studentId);
        int completed = 0;
        int inProgress = 0;
        int pending = 0;
        int skipped = 0;
        double completedHours = 0.0;
        double remainingHours = 0.0;
        int remainingWeeks = 0;

        StudentProfile projectedProfile = student.copyForSimulation();

        for (LearningPlanItem item : items) {
            double hours = item.getOpportunity() != null ? item.getOpportunity().getEstimatedHours() : 0.0;
            switch (item.getStatus()) {
                case COMPLETED -> {
                    completed++;
                    completedHours += hours;
                }
                case IN_PROGRESS -> {
                    inProgress++;
                    remainingHours += hours;
                    remainingWeeks += item.getTargetCompletionWeeks();
                }
                case PENDING -> {
                    pending++;
                    remainingHours += hours;
                    remainingWeeks += item.getTargetCompletionWeeks();
                }
                case SKIPPED -> skipped++;
            }

            if (item.getStatus() != PlanStatus.SKIPPED && item.getOpportunity() != null) {
                for (Skill s : item.getOpportunity().getSkillsGained()) {
                    StudentSkill existingSkill = projectedProfile.getSkillsMap().get(s.getName().trim().toLowerCase());
                    if (existingSkill == null || existingSkill.getProficiencyLevel().getNumericRank() < ProficiencyLevel.ADVANCED.getNumericRank()) {
                        projectedProfile.addSkill(new StudentSkill(
                                student.getId(), s, ProficiencyLevel.ADVANCED, 1.0, false
                        ));
                    }
                }
            }
        }

        SkillGapAnalysisDto currentGap = skillGapService.analyzeSkillGapForProfile(student, student.getTargetRole());
        SkillGapAnalysisDto projectedGap = skillGapService.analyzeSkillGapForProfile(projectedProfile, student.getTargetRole());

        WeeklyProgressSummaryDto summary = new WeeklyProgressSummaryDto();
        summary.setStudentId(studentId);
        summary.setTargetRole(student.getTargetRole());
        summary.setTotalItems(items.size());
        summary.setCompletedItems(completed);
        summary.setInProgressItems(inProgress);
        summary.setPendingItems(pending);
        summary.setSkippedItems(skipped);
        summary.setCompletedHours(Math.round(completedHours * 10.0) / 10.0);
        summary.setRemainingHours(Math.round(remainingHours * 10.0) / 10.0);
        summary.setRemainingWeeks(remainingWeeks);
        summary.setCurrentRoleMatchPercentage(currentGap.getMatchPercentage());
        summary.setProjectedRoleMatchPercentage(projectedGap.getMatchPercentage());
        summary.setEstimatedSkillCoverageGain(
                Math.round((projectedGap.getMatchPercentage() - currentGap.getMatchPercentage()) * 10.0) / 10.0
        );
        return summary;
    }

    /**
     * Quotes RFC-4180 CSV fields and neutralizes leading spreadsheet formula triggers.
     */
    private String escapeCsv(String raw) {
        if (raw == null) {
            return "\"\"";
        }
        String sanitized = raw.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ');
        if (!sanitized.isEmpty()) {
            char first = sanitized.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t') {
                sanitized = "'" + sanitized;
            }
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\"";
    }
}
