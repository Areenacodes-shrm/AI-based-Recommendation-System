package com.pathpilot.dto;

import com.pathpilot.model.DifficultyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class StudentProfileUpdateDto {

    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @Size(min = 2, max = 100, message = "Target role must be between 2 and 100 characters")
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
    private List<StudentSkillInputDto> skills;

    public StudentProfileUpdateDto() {}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

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

    public List<StudentSkillInputDto> getSkills() {
        return skills;
    }

    public void setSkills(List<StudentSkillInputDto> skills) {
        this.skills = skills;
    }
}
