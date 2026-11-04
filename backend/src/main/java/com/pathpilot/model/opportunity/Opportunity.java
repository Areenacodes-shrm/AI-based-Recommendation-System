package com.pathpilot.model.opportunity;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.OpportunityType;
import com.pathpilot.model.Skill;
import com.pathpilot.model.StudentProfile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class demonstrating OOP Inheritance and Encapsulation.
 * Captures foundational attributes and defines polymorphic contracts for all opportunities.
 */
public abstract class Opportunity {
    private Long id;
    private String title;
    private String description;
    private DifficultyLevel difficultyLevel;
    private double estimatedHours;
    private BigDecimal cost;
    private String provider;
    private String url;
    private double rating;
    private int ratingCount;
    private LocalDateTime createdAt;

    // Composition: Associated skills gained and prerequisite requirements
    private List<Skill> skillsGained = new ArrayList<>();
    private List<Skill> prerequisiteSkills = new ArrayList<>();

    public Opportunity() {
        this.cost = BigDecimal.ZERO;
        this.rating = 4.5;
        this.ratingCount = 0;
    }

    public Opportunity(Long id, String title, String description, DifficultyLevel difficultyLevel,
                       double estimatedHours, BigDecimal cost, String provider, String url) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "Title is mandatory");
        this.description = Objects.requireNonNull(description, "Description is mandatory");
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.INTERMEDIATE;
        this.estimatedHours = Math.max(1.0, estimatedHours);
        this.cost = (cost != null && cost.compareTo(BigDecimal.ZERO) >= 0) ? cost : BigDecimal.ZERO;
        this.provider = Objects.requireNonNull(provider, "Provider is mandatory");
        this.url = url;
        this.rating = 4.5;
        this.ratingCount = 0;
    }

    // Polymorphic contract: Subclasses must provide their specific OpportunityType
    public abstract OpportunityType getOpportunityType();

    // Polymorphic contract: Subclasses compute tailored difficulty penalties based on their nature
    public abstract double calculateDifficultyPenalty(StudentProfile student);

    // Polymorphic contract: Subclasses generate distinctive visual/catalog badges
    public abstract String getOpportunityBadge();

    // Reusable domain helpers demonstrating encapsulation
    public boolean isAffordable(BigDecimal budget) {
        if (budget == null) return true;
        return this.cost.compareTo(budget) <= 0;
    }

    public boolean fitsSchedule(int availableWeeklyHours) {
        // Subclasses may calculate week-based pacing; baseline ensures effort is non-zero
        return availableWeeklyHours > 0 && estimatedHours > 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = Math.max(1.0, estimatedHours);
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = (cost != null && cost.compareTo(BigDecimal.ZERO) >= 0) ? cost : BigDecimal.ZERO;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(5.0, rating));
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = Math.max(0, ratingCount);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Skill> getSkillsGained() {
        return skillsGained;
    }

    public void setSkillsGained(List<Skill> skillsGained) {
        this.skillsGained = skillsGained != null ? skillsGained : new ArrayList<>();
    }

    public List<Skill> getPrerequisiteSkills() {
        return prerequisiteSkills;
    }

    public void setPrerequisiteSkills(List<Skill> prerequisiteSkills) {
        this.prerequisiteSkills = prerequisiteSkills != null ? prerequisiteSkills : new ArrayList<>();
    }
}
