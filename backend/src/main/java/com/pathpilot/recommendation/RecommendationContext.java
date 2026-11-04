package com.pathpilot.recommendation;

import com.pathpilot.model.Skill;
import com.pathpilot.model.StudentSkill;
import com.pathpilot.model.TargetRoleSkill;

import java.util.*;

/**
 * Context object carrying contextual weights, target role benchmark skills,
 * and precomputed caches during a recommendation cycle.
 */
public class RecommendationContext {
    private final String targetRole;
    private final Map<String, TargetRoleSkill> roleSkillsMap = new HashMap<>();
    private final Map<String, StudentSkill> studentSkillsMap = new HashMap<>();
    private final Map<Long, Double> opportunityFeedbackRatings = new HashMap<>();
    private final Set<Long> studentInteractedOpportunityIds = new HashSet<>();

    // Scoring Weights (sum = 1.0)
    private double contentWeight = 0.20;
    private double skillWeight = 0.35;
    private double careerWeight = 0.25;
    private double contextualWeight = 0.10;
    private double feedbackWeight = 0.10;

    public RecommendationContext(String targetRole) {
        this.targetRole = targetRole;
    }

    public void setRoleSkills(Collection<TargetRoleSkill> roleSkills) {
        roleSkillsMap.clear();
        if (roleSkills != null) {
            for (TargetRoleSkill trs : roleSkills) {
                if (trs.getSkill() != null) {
                    roleSkillsMap.put(trs.getSkill().getName().trim().toLowerCase(), trs);
                }
            }
        }
    }

    public void setStudentSkills(Collection<StudentSkill> studentSkills) {
        studentSkillsMap.clear();
        if (studentSkills != null) {
            for (StudentSkill ss : studentSkills) {
                if (ss.getSkill() != null) {
                    studentSkillsMap.put(ss.getSkill().getName().trim().toLowerCase(), ss);
                }
            }
        }
    }

    public String getTargetRole() {
        return targetRole;
    }

    public Map<String, TargetRoleSkill> getRoleSkillsMap() {
        return Collections.unmodifiableMap(roleSkillsMap);
    }

    public Map<String, StudentSkill> getStudentSkillsMap() {
        return Collections.unmodifiableMap(studentSkillsMap);
    }

    public Map<Long, Double> getOpportunityFeedbackRatings() {
        return opportunityFeedbackRatings;
    }

    public void setOpportunityFeedbackRating(Long oppId, double rating) {
        opportunityFeedbackRatings.put(oppId, rating);
    }

    public Set<Long> getStudentInteractedOpportunityIds() {
        return studentInteractedOpportunityIds;
    }

    public void addInteractedOpportunity(Long oppId) {
        studentInteractedOpportunityIds.add(oppId);
    }

    public double getContentWeight() {
        return contentWeight;
    }

    public void setContentWeight(double contentWeight) {
        this.contentWeight = contentWeight;
    }

    public double getSkillWeight() {
        return skillWeight;
    }

    public void setSkillWeight(double skillWeight) {
        this.skillWeight = skillWeight;
    }

    public double getCareerWeight() {
        return careerWeight;
    }

    public void setCareerWeight(double careerWeight) {
        this.careerWeight = careerWeight;
    }

    public double getContextualWeight() {
        return contextualWeight;
    }

    public void setContextualWeight(double contextualWeight) {
        this.contextualWeight = contextualWeight;
    }

    public double getFeedbackWeight() {
        return feedbackWeight;
    }

    public void setFeedbackWeight(double feedbackWeight) {
        this.feedbackWeight = feedbackWeight;
    }
}
