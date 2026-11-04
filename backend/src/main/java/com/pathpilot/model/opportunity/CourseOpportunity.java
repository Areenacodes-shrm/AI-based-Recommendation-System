package com.pathpilot.model.opportunity;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.StudentProfile;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Specialized Opportunity subclass representing structured courses and guided curricula.
 */
public class CourseOpportunity extends Opportunity {
    private String platform;
    private String syllabusSummary;
    private boolean certificateIncluded;
    private int durationWeeks;

    public CourseOpportunity() {
        super();
        this.certificateIncluded = true;
        this.durationWeeks = 4;
    }

    public CourseOpportunity(Long id, String title, String description, DifficultyLevel difficultyLevel,
                             double estimatedHours, BigDecimal cost, String provider, String url,
                             String platform, String syllabusSummary, boolean certificateIncluded,
                             int durationWeeks) {
        super(id, title, description, difficultyLevel, estimatedHours, cost, provider, url);
        this.platform = Objects.requireNonNull(platform, "Platform cannot be null");
        this.syllabusSummary = syllabusSummary;
        this.certificateIncluded = certificateIncluded;
        this.durationWeeks = Math.max(1, durationWeeks);
    }

    @Override
    public OpportunityType getOpportunityType() {
        return OpportunityType.COURSE;
    }

    @Override
    public double calculateDifficultyPenalty(StudentProfile student) {
        if (student == null || student.getPreferredDifficulty() == null) return 0.0;
        int diffDelta = getDifficultyLevel().getNumericRank() - student.getPreferredDifficulty().getNumericRank();
        // Courses provide structured pedagogical scaffolding; slight penalty if 2+ tiers above
        if (diffDelta > 1) {
            return 0.15 * (diffDelta - 1);
        }
        return 0.0;
    }

    @Override
    public String getOpportunityBadge() {
        return "Course • " + (platform != null ? platform : getProvider());
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSyllabusSummary() {
        return syllabusSummary;
    }

    public void setSyllabusSummary(String syllabusSummary) {
        this.syllabusSummary = syllabusSummary;
    }

    public boolean isCertificateIncluded() {
        return certificateIncluded;
    }

    public void setCertificateIncluded(boolean certificateIncluded) {
        this.certificateIncluded = certificateIncluded;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = Math.max(1, durationWeeks);
    }
}
