package com.pathpilot.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Encapsulated domain model representing a student profile, career aspiration,
 * personal constraints, and current skill competencies.
 */
public class StudentProfile {
    private Long id;
    private String fullName;
    private String email;
    private String targetRole;
    private int weeklyHoursAvailable;
    private BigDecimal maxBudget;
    private DifficultyLevel preferredDifficulty;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Encapsulated map for fast skill lookup by lowercase skill name (O(1) access)
    private final Map<String, StudentSkill> skillsMap = new HashMap<>();

    public StudentProfile() {
        this.weeklyHoursAvailable = 15;
        this.maxBudget = BigDecimal.valueOf(100.00);
        this.preferredDifficulty = DifficultyLevel.INTERMEDIATE;
    }

    public StudentProfile(Long id, String fullName, String email, String targetRole,
                          int weeklyHoursAvailable, BigDecimal maxBudget,
                          DifficultyLevel preferredDifficulty, String bio) {
        this.id = id;
        this.fullName = Objects.requireNonNull(fullName, "Full name is required");
        this.email = Objects.requireNonNull(email, "Email is required");
        this.targetRole = Objects.requireNonNull(targetRole, "Target role is required");
        setWeeklyHoursAvailable(weeklyHoursAvailable);
        setMaxBudget(maxBudget);
        this.preferredDifficulty = preferredDifficulty != null ? preferredDifficulty : DifficultyLevel.INTERMEDIATE;
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        // Enforce valid time boundary
        this.weeklyHoursAvailable = Math.max(1, Math.min(80, weeklyHoursAvailable));
    }

    public BigDecimal getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(BigDecimal maxBudget) {
        // Prevent negative budget constraints
        this.maxBudget = (maxBudget != null && maxBudget.compareTo(BigDecimal.ZERO) >= 0)
                ? maxBudget : BigDecimal.ZERO;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, StudentSkill> getSkillsMap() {
        return Collections.unmodifiableMap(skillsMap);
    }

    public List<StudentSkill> getSkillsList() {
        return new ArrayList<>(skillsMap.values());
    }

    public void addSkill(StudentSkill studentSkill) {
        if (studentSkill != null && studentSkill.getSkill() != null) {
            skillsMap.put(studentSkill.getSkill().getName().trim().toLowerCase(), studentSkill);
        }
    }

    public void setSkills(Collection<StudentSkill> skills) {
        this.skillsMap.clear();
        if (skills != null) {
            for (StudentSkill s : skills) {
                addSkill(s);
            }
        }
    }

    public boolean hasSkill(String skillName) {
        if (skillName == null) return false;
        return skillsMap.containsKey(skillName.trim().toLowerCase());
    }

    public ProficiencyLevel getProficiencyFor(String skillName) {
        if (skillName == null) return null;
        StudentSkill ss = skillsMap.get(skillName.trim().toLowerCase());
        return ss != null ? ss.getProficiencyLevel() : null;
    }
}
