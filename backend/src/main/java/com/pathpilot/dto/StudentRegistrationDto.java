package com.pathpilot.dto;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.ProficiencyLevel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StudentRegistrationDto {
    private String fullName;
    private String email;
    private String targetRole;
    private int weeklyHoursAvailable = 15;
    private BigDecimal maxBudget = BigDecimal.valueOf(100.00);
    private DifficultyLevel preferredDifficulty = DifficultyLevel.INTERMEDIATE;
    private String bio;
    private List<StudentSkillInputDto> skills = new ArrayList<>();

    public StudentRegistrationDto() {}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getWeeklyHoursAvailable() {
        return weeklyHoursAvailable;
    }

    public void setWeeklyHoursAvailable(int weeklyHoursAvailable) {
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
        this.skills = skills != null ? skills : new ArrayList<>();
    }
}
