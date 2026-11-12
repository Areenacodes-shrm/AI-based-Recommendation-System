package com.pathpilot.dto;

import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RelevanceBasis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response payload showing side-by-side differences between the persisted baseline profile
 * and a temporary what-if profile simulation.
 */
public class WhatIfSimulationResultDto {
    private Long studentId;
    private String baselineTargetRole;
    private String simulatedTargetRole;
    private int baselineWeeklyHours;
    private int simulatedWeeklyHours;
    private BigDecimal baselineMaxBudget;
    private BigDecimal simulatedMaxBudget;
    private DifficultyLevel simulatedDifficulty;
    private double baselineRoleMatchPercentage;
    private double simulatedRoleMatchPercentage;
    private double roleMatchDelta;
    private RelevanceBasis relevanceBasis;
    private boolean persisted;
    private List<SimulationItemDeltaDto> items = new ArrayList<>();

    public static class SimulationItemDeltaDto {
        private RecommendationItem item;
        private Integer baselineRank;
        private int simulatedRank;
        private Integer rankDelta;
        private double baselineScore;
        private double scoreDelta;
        private String changeStatus;

        public SimulationItemDeltaDto() {}

        public SimulationItemDeltaDto(RecommendationItem item, Integer baselineRank, int simulatedRank,
                                      double baselineScore, double scoreDelta, String changeStatus) {
            this.item = item;
            this.baselineRank = baselineRank;
            this.simulatedRank = simulatedRank;
            this.rankDelta = baselineRank != null ? (baselineRank - simulatedRank) : null;
            this.baselineScore = baselineScore;
            this.scoreDelta = Math.round(scoreDelta * 1000.0) / 1000.0;
            this.changeStatus = changeStatus;
        }

        public RecommendationItem getItem() {
            return item;
        }

        public void setItem(RecommendationItem item) {
            this.item = item;
        }

        public Integer getBaselineRank() {
            return baselineRank;
        }

        public void setBaselineRank(Integer baselineRank) {
            this.baselineRank = baselineRank;
        }

        public int getSimulatedRank() {
            return simulatedRank;
        }

        public void setSimulatedRank(int simulatedRank) {
            this.simulatedRank = simulatedRank;
        }

        public Integer getRankDelta() {
            return rankDelta;
        }

        public void setRankDelta(Integer rankDelta) {
            this.rankDelta = rankDelta;
        }

        public double getBaselineScore() {
            return baselineScore;
        }

        public void setBaselineScore(double baselineScore) {
            this.baselineScore = baselineScore;
        }

        public double getScoreDelta() {
            return scoreDelta;
        }

        public void setScoreDelta(double scoreDelta) {
            this.scoreDelta = scoreDelta;
        }

        public String getChangeStatus() {
            return changeStatus;
        }

        public void setChangeStatus(String changeStatus) {
            this.changeStatus = changeStatus;
        }
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getBaselineTargetRole() {
        return baselineTargetRole;
    }

    public void setBaselineTargetRole(String baselineTargetRole) {
        this.baselineTargetRole = baselineTargetRole;
    }

    public String getSimulatedTargetRole() {
        return simulatedTargetRole;
    }

    public void setSimulatedTargetRole(String simulatedTargetRole) {
        this.simulatedTargetRole = simulatedTargetRole;
    }

    public int getBaselineWeeklyHours() {
        return baselineWeeklyHours;
    }

    public void setBaselineWeeklyHours(int baselineWeeklyHours) {
        this.baselineWeeklyHours = baselineWeeklyHours;
    }

    public int getSimulatedWeeklyHours() {
        return simulatedWeeklyHours;
    }

    public void setSimulatedWeeklyHours(int simulatedWeeklyHours) {
        this.simulatedWeeklyHours = simulatedWeeklyHours;
    }

    public BigDecimal getBaselineMaxBudget() {
        return baselineMaxBudget;
    }

    public void setBaselineMaxBudget(BigDecimal baselineMaxBudget) {
        this.baselineMaxBudget = baselineMaxBudget;
    }

    public BigDecimal getSimulatedMaxBudget() {
        return simulatedMaxBudget;
    }

    public void setSimulatedMaxBudget(BigDecimal simulatedMaxBudget) {
        this.simulatedMaxBudget = simulatedMaxBudget;
    }

    public DifficultyLevel getSimulatedDifficulty() {
        return simulatedDifficulty;
    }

    public void setSimulatedDifficulty(DifficultyLevel simulatedDifficulty) {
        this.simulatedDifficulty = simulatedDifficulty;
    }

    public double getBaselineRoleMatchPercentage() {
        return baselineRoleMatchPercentage;
    }

    public void setBaselineRoleMatchPercentage(double baselineRoleMatchPercentage) {
        this.baselineRoleMatchPercentage = baselineRoleMatchPercentage;
    }

    public double getSimulatedRoleMatchPercentage() {
        return simulatedRoleMatchPercentage;
    }

    public void setSimulatedRoleMatchPercentage(double simulatedRoleMatchPercentage) {
        this.simulatedRoleMatchPercentage = simulatedRoleMatchPercentage;
    }

    public double getRoleMatchDelta() {
        return roleMatchDelta;
    }

    public void setRoleMatchDelta(double roleMatchDelta) {
        this.roleMatchDelta = roleMatchDelta;
    }

    public RelevanceBasis getRelevanceBasis() {
        return relevanceBasis;
    }

    public void setRelevanceBasis(RelevanceBasis relevanceBasis) {
        this.relevanceBasis = relevanceBasis;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public List<SimulationItemDeltaDto> getItems() {
        return items;
    }

    public void setItems(List<SimulationItemDeltaDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
