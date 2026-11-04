package com.pathpilot.dto;

import com.pathpilot.model.ProficiencyLevel;

import java.util.ArrayList;
import java.util.List;

public class SkillGapAnalysisDto {
    private String targetRole;
    private double matchPercentage;
    private int totalRequiredSkills;
    private int satisfiedSkillsCount;
    private List<SkillItemDto> matchedSkills = new ArrayList<>();
    private List<SkillItemDto> weakSkills = new ArrayList<>();
    private List<SkillItemDto> missingSkills = new ArrayList<>();

    public SkillGapAnalysisDto() {}

    public static class SkillItemDto {
        private String skillName;
        private String category;
        private ProficiencyLevel currentProficiency;
        private ProficiencyLevel requiredProficiency;
        private double importanceWeight;
        private boolean isCore;

        public SkillItemDto() {}

        public SkillItemDto(String skillName, String category, ProficiencyLevel currentProficiency,
                            ProficiencyLevel requiredProficiency, double importanceWeight, boolean isCore) {
            this.skillName = skillName;
            this.category = category;
            this.currentProficiency = currentProficiency;
            this.requiredProficiency = requiredProficiency;
            this.importanceWeight = importanceWeight;
            this.isCore = isCore;
        }

        public String getSkillName() {
            return skillName;
        }

        public void setSkillName(String skillName) {
            this.skillName = skillName;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public ProficiencyLevel getCurrentProficiency() {
            return currentProficiency;
        }

        public void setCurrentProficiency(ProficiencyLevel currentProficiency) {
            this.currentProficiency = currentProficiency;
        }

        public ProficiencyLevel getRequiredProficiency() {
            return requiredProficiency;
        }

        public void setRequiredProficiency(ProficiencyLevel requiredProficiency) {
            this.requiredProficiency = requiredProficiency;
        }

        public double getImportanceWeight() {
            return importanceWeight;
        }

        public void setImportanceWeight(double importanceWeight) {
            this.importanceWeight = importanceWeight;
        }

        public boolean isCore() {
            return isCore;
        }

        public void setCore(boolean core) {
            this.isCore = core;
        }
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public int getTotalRequiredSkills() {
        return totalRequiredSkills;
    }

    public void setTotalRequiredSkills(int totalRequiredSkills) {
        this.totalRequiredSkills = totalRequiredSkills;
    }

    public int getSatisfiedSkillsCount() {
        return satisfiedSkillsCount;
    }

    public void setSatisfiedSkillsCount(int satisfiedSkillsCount) {
        this.satisfiedSkillsCount = satisfiedSkillsCount;
    }

    public List<SkillItemDto> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<SkillItemDto> matchedSkills) {
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
    }

    public List<SkillItemDto> getWeakSkills() {
        return weakSkills;
    }

    public void setWeakSkills(List<SkillItemDto> weakSkills) {
        this.weakSkills = weakSkills != null ? weakSkills : new ArrayList<>();
    }

    public List<SkillItemDto> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<SkillItemDto> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }
}
