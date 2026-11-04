package com.pathpilot.dto;

import com.pathpilot.model.ProficiencyLevel;

public class StudentSkillInputDto {
    private Long skillId;
    private String skillName;
    private ProficiencyLevel proficiencyLevel;
    private double yearsExperience;

    public StudentSkillInputDto() {
        this.proficiencyLevel = ProficiencyLevel.BEGINNER;
        this.yearsExperience = 0.5;
    }

    public StudentSkillInputDto(Long skillId, String skillName, ProficiencyLevel proficiencyLevel, double yearsExperience) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.proficiencyLevel = proficiencyLevel;
        this.yearsExperience = yearsExperience;
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
