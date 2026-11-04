package com.pathpilot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Transparent explanation breakdown accompanying each recommended opportunity.
 * Fully details why the opportunity was selected, skill overlaps, missing skills, and scheduling fit.
 */
public class ExplanationData {
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private String careerRelevanceReason;
    private String timeAndBudgetFitReason;
    private String prerequisitesReason;
    private String difficultyAssessment;

    public ExplanationData() {}

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }

    public String getCareerRelevanceReason() {
        return careerRelevanceReason;
    }

    public void setCareerRelevanceReason(String careerRelevanceReason) {
        this.careerRelevanceReason = careerRelevanceReason;
    }

    public String getTimeAndBudgetFitReason() {
        return timeAndBudgetFitReason;
    }

    public void setTimeAndBudgetFitReason(String timeAndBudgetFitReason) {
        this.timeAndBudgetFitReason = timeAndBudgetFitReason;
    }

    public String getPrerequisitesReason() {
        return prerequisitesReason;
    }

    public void setPrerequisitesReason(String prerequisitesReason) {
        this.prerequisitesReason = prerequisitesReason;
    }

    public String getDifficultyAssessment() {
        return difficultyAssessment;
    }

    public void setDifficultyAssessment(String difficultyAssessment) {
        this.difficultyAssessment = difficultyAssessment;
    }
}
