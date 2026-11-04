package com.pathpilot.model.opportunity;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.LocationType;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.StudentProfile;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Specialized Opportunity subclass representing competitive industry internships.
 */
public class InternshipOpportunity extends Opportunity {
    private String companyName;
    private BigDecimal monthlyStipend;
    private LocationType locationType;
    private int durationMonths;
    private String applicationDeadline;

    public InternshipOpportunity() {
        super();
        this.monthlyStipend = BigDecimal.ZERO;
        this.locationType = LocationType.REMOTE;
        this.durationMonths = 3;
    }

    public InternshipOpportunity(Long id, String title, String description, DifficultyLevel difficultyLevel,
                                 double estimatedHours, BigDecimal cost, String provider, String url,
                                 String companyName, BigDecimal monthlyStipend, LocationType locationType,
                                 int durationMonths, String applicationDeadline) {
        super(id, title, description, difficultyLevel, estimatedHours, cost, provider, url);
        this.companyName = Objects.requireNonNull(companyName, "Company name cannot be null");
        this.monthlyStipend = monthlyStipend != null ? monthlyStipend : BigDecimal.ZERO;
        this.locationType = locationType != null ? locationType : LocationType.REMOTE;
        this.durationMonths = Math.max(1, durationMonths);
        this.applicationDeadline = applicationDeadline;
    }

    @Override
    public OpportunityType getOpportunityType() {
        return OpportunityType.INTERNSHIP;
    }

    @Override
    public double calculateDifficultyPenalty(StudentProfile student) {
        if (student == null || student.getPreferredDifficulty() == null) return 0.0;
        int diffDelta = getDifficultyLevel().getNumericRank() - student.getPreferredDifficulty().getNumericRank();
        // Internships have strict hiring bars; penalty is substantial if student does not meet difficulty level
        if (diffDelta > 0) {
            return 0.25 * diffDelta;
        }
        return 0.0;
    }

    @Override
    public String getOpportunityBadge() {
        return "Internship • " + (companyName != null ? companyName : getProvider());
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getMonthlyStipend() {
        return monthlyStipend;
    }

    public void setMonthlyStipend(BigDecimal monthlyStipend) {
        this.monthlyStipend = monthlyStipend != null ? monthlyStipend : BigDecimal.ZERO;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = Math.max(1, durationMonths);
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
}
