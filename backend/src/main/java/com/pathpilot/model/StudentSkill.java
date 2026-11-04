package com.pathpilot.model;

import java.util.Objects;

/**
 * Associates a student with a specific skill and proficiency rating.
 */
public class StudentSkill {
    private Long studentId;
    private Skill skill;
    private ProficiencyLevel proficiencyLevel;
    private double yearsExperience;
    private boolean verified;

    public StudentSkill() {}

    public StudentSkill(Long studentId, Skill skill, ProficiencyLevel proficiencyLevel, double yearsExperience, boolean verified) {
        this.studentId = studentId;
        this.skill = Objects.requireNonNull(skill, "Skill cannot be null");
        this.proficiencyLevel = Objects.requireNonNull(proficiencyLevel, "Proficiency cannot be null");
        this.yearsExperience = Math.max(0.0, yearsExperience);
        this.verified = verified;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public String toString() {
        return "StudentSkill{" +
                "skill=" + (skill != null ? skill.getName() : "null") +
                ", level=" + proficiencyLevel +
                ", verified=" + verified +
                '}';
    }
}
