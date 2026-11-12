package com.pathpilot.dto;

import com.pathpilot.model.DifficultyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Hypothetical profile adjustments for non-destructive What-If Career Simulation.
 */
public class WhatIfSimulationRequestDto {

    @Size(max = 100, message = "Target role must not exceed 100 characters")
    private String targetRole;

    @Min(value = 1, message = "Weekly hours available must be between 1 and 80")
    @Max(value = 80, message = "Weekly hours available must be between 1 and 80")
    private Integer weeklyHoursAvailable;

    @DecimalMin(value = "0.00", message = "Max budget cannot be negative")
    private BigDecimal maxBudget;

    private DifficultyLevel preferredDifficulty;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    @Valid
    private List<StudentSkillInputDto> simulatedSkills;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 25, message = "Limit cannot exceed 25")
    private int limit = 8;

    private boolean applyDiversity = true;

    public WhatIfSimulationRequestDto() {}

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Integer getWeeklyHoursAvailable() {
        return weeklyHoursAvailable;
    }

    public void setWeeklyHoursAvailable(Integer weeklyHoursAvailable) {
        this.weeklyHoursAvailable = weeklyHoursAvailable;
    }

    public BigDecimal getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(BigDecimal maxBudget) {
        this.maxBudget = maxBudget;
    }

    public DifficultyLevel getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public void setPreferredDifficulty(DifficultyLevel preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<StudentSkillInputDto> getSimulatedSkills() {
        return simulatedSkills;
    }

    public void setSimulatedSkills(List<StudentSkillInputDto> simulatedSkills) {
        this.simulatedSkills = simulatedSkills;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isApplyDiversity() {
        return applyDiversity;
    }

    public void setApplyDiversity(boolean applyDiversity) {
        this.applyDiversity = applyDiversity;
    }
}
