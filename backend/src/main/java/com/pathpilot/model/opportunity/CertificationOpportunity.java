package com.pathpilot.model.opportunity;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.StudentProfile;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Specialized Opportunity subclass representing formal industry credentials and proctored certifications.
 */
public class CertificationOpportunity extends Opportunity {
    private String issuingOrganization;
    private String examCode;
    private int validityYears;
    private String examFormat;

    public CertificationOpportunity() {
        super();
        this.validityYears = 3;
    }

    public CertificationOpportunity(Long id, String title, String description, DifficultyLevel difficultyLevel,
                                    double estimatedHours, BigDecimal cost, String provider, String url,
                                    String issuingOrganization, String examCode, int validityYears,
                                    String examFormat) {
        super(id, title, description, difficultyLevel, estimatedHours, cost, provider, url);
        this.issuingOrganization = Objects.requireNonNull(issuingOrganization, "Issuing organization cannot be null");
        this.examCode = examCode;
        this.validityYears = Math.max(1, validityYears);
        this.examFormat = examFormat;
    }

    @Override
    public OpportunityType getOpportunityType() {
        return OpportunityType.CERTIFICATION;
    }

    @Override
    public double calculateDifficultyPenalty(StudentProfile student) {
        if (student == null || student.getPreferredDifficulty() == null) return 0.0;
        int diffDelta = getDifficultyLevel().getNumericRank() - student.getPreferredDifficulty().getNumericRank();
        // Standardized certifications strictly evaluate proficiency
        if (diffDelta > 0) {
            return 0.18 * diffDelta;
        }
        return 0.0;
    }

    @Override
    public String getOpportunityBadge() {
        return "Cert • " + (examCode != null ? examCode : issuingOrganization);
    }

    public String getIssuingOrganization() {
        return issuingOrganization;
    }

    public void setIssuingOrganization(String issuingOrganization) {
        this.issuingOrganization = issuingOrganization;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public int getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(int validityYears) {
        this.validityYears = Math.max(1, validityYears);
    }

    public String getExamFormat() {
        return examFormat;
    }

    public void setExamFormat(String examFormat) {
        this.examFormat = examFormat;
    }
}
