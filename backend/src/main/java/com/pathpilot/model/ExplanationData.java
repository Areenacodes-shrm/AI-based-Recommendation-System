package com.pathpilot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Transparent explanation breakdown accompanying each recommended opportunity.
 * Fully details why the opportunity was selected, skill overlaps, missing skills,
 * scheduling fit, and data-basis confidence indicators.
 */
public class ExplanationData {
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private String careerRelevanceReason;
    private String timeAndBudgetFitReason;
    private String prerequisitesReason;
    private String difficultyAssessment;
    private RelevanceBasis relevanceBasis = RelevanceBasis.PROFILE_BASED;
    private String confidenceLabel;
    private String confidenceNote;

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

    public RelevanceBasis getRelevanceBasis() {
        return relevanceBasis;
    }

    public void setRelevanceBasis(RelevanceBasis relevanceBasis) {
        this.relevanceBasis = relevanceBasis;
    }

    public String getConfidenceLabel() {
        return confidenceLabel;
    }

    public void setConfidenceLabel(String confidenceLabel) {
        this.confidenceLabel = confidenceLabel;
    }

    public String getConfidenceNote() {
        return confidenceNote;
    }

    public void setConfidenceNote(String confidenceNote) {
        this.confidenceNote = confidenceNote;
    }
}
