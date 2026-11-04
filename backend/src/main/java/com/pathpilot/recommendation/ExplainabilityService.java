package com.pathpilot.recommendation;

import com.pathpilot.model.*;
import com.pathpilot.model.opportunity.Opportunity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates transparent, human-interpretable justifications for each recommended opportunity.
 */
@Service
public class ExplainabilityService {

    public ExplanationData generateExplanation(StudentProfile student, Opportunity opp, RecommendationContext context) {
        ExplanationData exp = new ExplanationData();

        Map<String, StudentSkill> studentSkills = context != null
                ? context.getStudentSkillsMap()
                : student.getSkillsMap();

        Map<String, TargetRoleSkill> roleSkills = context != null
                ? context.getRoleSkillsMap()
                : Map.of();

        // 1. Matched Skills (Gained skills relevant to the target role)
        List<String> matched = new ArrayList<>();
        for (Skill s : opp.getSkillsGained()) {
            String nameLower = s.getName().toLowerCase();
            if (roleSkills.containsKey(nameLower)) {
                matched.add(s.getName() + " (Core Role Skill)");
            } else if (studentSkills.containsKey(nameLower)) {
                matched.add(s.getName() + " (Skill Upgrade)");
            } else {
                matched.add(s.getName());
            }
        }
        exp.setMatchedSkills(matched);

        // 2. Missing Skills (Target role requirements not covered by this opportunity)
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, TargetRoleSkill> entry : roleSkills.entrySet()) {
            String roleSkillName = entry.getValue().getSkill().getName();
            boolean studentHasIt = studentSkills.containsKey(entry.getKey());
            boolean oppTeachesIt = opp.getSkillsGained().stream()
                    .anyMatch(g -> g.getName().equalsIgnoreCase(roleSkillName));

            if (!studentHasIt && !oppTeachesIt && entry.getValue().isCore()) {
                missing.add(roleSkillName);
            }
        }
        exp.setMissingSkills(missing.stream().limit(4).toList());

        // 3. Career Relevance Reason
        long coreRoleSkillsTaught = opp.getSkillsGained().stream()
                .filter(g -> roleSkills.containsKey(g.getName().toLowerCase()))
                .count();

        if (coreRoleSkillsTaught > 0) {
            exp.setCareerRelevanceReason(String.format(
                    "Directly advances your goal for %s by teaching %d essential industry skills.",
                    student.getTargetRole(), coreRoleSkillsTaught));
        } else {
            exp.setCareerRelevanceReason(String.format(
                    "Provides foundational competencies supporting your broader progression in %s.",
                    student.getTargetRole()));
        }

        // 4. Time & Budget Fit Reason
        BigDecimal cost = opp.getCost();
        String budgetText = (cost.compareTo(BigDecimal.ZERO) == 0)
                ? "100% Free / Open Access"
                : String.format("$%.2f (Within your $%.2f budget limit)", cost, student.getMaxBudget());

        double hoursPerWeek = opp.getEstimatedHours() / 6.0;
        String timeText = String.format("~%.1f hrs/wk needed (Your weekly capacity: %d hrs)",
                hoursPerWeek, student.getWeeklyHoursAvailable());

        exp.setTimeAndBudgetFitReason(budgetText + " • " + timeText);

        // 5. Prerequisites Reason
        List<Skill> prereqs = opp.getPrerequisiteSkills();
        if (prereqs.isEmpty()) {
            exp.setPrerequisitesReason("No prior prerequisites required. Direct beginner-friendly enrollment.");
        } else {
            long satisfied = prereqs.stream()
                    .filter(p -> studentSkills.containsKey(p.getName().toLowerCase()))
                    .count();
            if (satisfied == prereqs.size()) {
                exp.setPrerequisitesReason("All prerequisite skills satisfied based on your verified background.");
            } else {
                exp.setPrerequisitesReason(String.format("Satisfies %d of %d prerequisites. Introductory review recommended.",
                        satisfied, prereqs.size()));
            }
        }

        // 6. Difficulty Assessment
        int delta = opp.getDifficultyLevel().getNumericRank() - student.getPreferredDifficulty().getNumericRank();
        if (delta == 0) {
            exp.setDifficultyAssessment("Exact match with your current comfort and preferred difficulty.");
        } else if (delta > 0) {
            exp.setDifficultyAssessment(String.format("Challenging growth track (+%d level above preference).", delta));
        } else {
            exp.setDifficultyAssessment("Accessible refresher level for rapid completion and confidence building.");
        }

        return exp;
    }
}
