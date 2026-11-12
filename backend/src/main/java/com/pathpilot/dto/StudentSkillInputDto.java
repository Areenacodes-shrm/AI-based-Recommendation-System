package com.pathpilot.dto;

import com.pathpilot.model.ProficiencyLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class StudentSkillInputDto {
    private Long skillId;
    private String skillName;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiencyLevel;

    @DecimalMin(value = "0.0", message = "Years experience cannot be negative")
    private double yearsExperience;

    public StudentSkillInputDto() {
        this.proficiencyLevel = ProficiencyLevel.BEGINNER;
        this.yearsExperience = 0.5;
    }

    public StudentSkillInputDto(Long skillId, String skillName, ProficiencyLevel proficiencyLevel, double yearsExperience) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel != null ? proficiencyLevel : ProficiencyLevel.BEGINNER;
        this.yearsExperience = Math.max(0.0, yearsExperience);
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public double getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(double yearsExperience) {
        this.yearsExperience = yearsExperience;
    }
}
