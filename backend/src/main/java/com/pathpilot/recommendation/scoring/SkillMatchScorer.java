package com.pathpilot.recommendation.scoring;

import com.pathpilot.model.ProficiencyLevel;
import com.pathpilot.model.Skill;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.model.StudentSkill;
import com.pathpilot.model.TargetRoleSkill;
import com.pathpilot.model.opportunity.Opportunity;
import com.pathpilot.recommendation.RecommendationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Computes skill alignment score evaluating:
 * 1. Prerequisites satisfaction (Does student have required background?)
 * 2. Growth potential (Does opportunity teach skills needed for the student's target role?)
 */
@Component
public class SkillMatchScorer implements RecommendationScorer {

    @Override
    public double score(StudentProfile student, Opportunity opportunity, RecommendationContext context) {
        if (student == null || opportunity == null) return 0.0;

        List<Skill> prereqs = opportunity.getPrerequisiteSkills();
        List<Skill> gained = opportunity.getSkillsGained();

        Map<String, StudentSkill> studentSkills = context != null
                ? context.getStudentSkillsMap()
                : student.getSkillsMap();

        Map<String, TargetRoleSkill> targetRoleSkills = context != null
                ? context.getRoleSkillsMap()
                : Map.of();

        // 1. Prerequisite Readiness: Ratio of required skills student currently holds
        double prereqScore = 1.0;
        if (!prereqs.isEmpty()) {
            int satisfiedCount = 0;
            for (Skill req : prereqs) {
                String reqName = req.getName().toLowerCase();
                if (studentSkills.containsKey(reqName)) {
                    satisfiedCount++;
                }
            }
            prereqScore = (double) satisfiedCount / prereqs.size();
        }

        // 2. Skill Growth Relevance: How many skills taught by this opportunity advance target role?
        double growthScore = 0.20; // Fallback baseline
        if (!gained.isEmpty()) {
            double totalWeight = 0.0;
            double matchedWeight = 0.0;

            for (Skill g : gained) {
                String gName = g.getName().toLowerCase();
                totalWeight += 1.0;

                // Check if target role requires this skill
                if (targetRoleSkills.containsKey(gName)) {
                    TargetRoleSkill trs = targetRoleSkills.get(gName);
                    double weight = trs.getImportanceWeight();

                    // If student already has this skill at expert level, diminished marginal gain
                    StudentSkill existing = studentSkills.get(gName);
                    if (existing != null && existing.getProficiencyLevel() == ProficiencyLevel.EXPERT) {
                        matchedWeight += weight * 0.2;
                    } else if (existing != null) {
                        // Opportunity levels up existing skill
                        matchedWeight += weight * 0.85;
                    } else {
                        // Opportunity closes an entirely missing skill gap
                        matchedWeight += weight * 1.0;
                    }
                } else if (!studentSkills.containsKey(gName)) {
                    // Valuable auxiliary skill
                    matchedWeight += 0.4;
                }
            }

            growthScore = totalWeight > 0 ? (matchedWeight / totalWeight) : 0.2;
        }

        // Hybrid harmonic combination: Prioritize growth, modulated by prerequisite readiness
        double combined = (0.35 * prereqScore) + (0.65 * growthScore);
        return Math.max(0.0, Math.min(1.0, combined));
    }

    @Override
    public String getScorerName() {
        return "SkillMatchScorer";
    }
}
