package com.pathpilot.dto;

import com.pathpilot.model.DifficultyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StudentRegistrationDto {

    @NotBlank(message = "Full name must not be blank")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Target role must not be blank")
    @Size(max = 100, message = "Target role must not exceed 100 characters")
    private String targetRole;

    @Min(value = 1, message = "Weekly hours available must be at least 1")
    @Max(value = 80, message = "Weekly hours available cannot exceed 80")
    private int weeklyHoursAvailable = 15;

    @NotNull(message = "Max budget is required")
    @DecimalMin(value = "0.00", message = "Max budget cannot be negative")
    private BigDecimal maxBudget = BigDecimal.valueOf(100.00);

    private DifficultyLevel preferredDifficulty = DifficultyLevel.INTERMEDIATE;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    @Valid
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
