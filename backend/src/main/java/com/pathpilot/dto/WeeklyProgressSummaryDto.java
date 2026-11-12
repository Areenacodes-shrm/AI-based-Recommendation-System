package com.pathpilot.dto;

/**
 * Lightweight weekly progress summary derived from persisted roadmap progress and role benchmarks.
 */
public class WeeklyProgressSummaryDto {
    private Long studentId;
    private String targetRole;
    private int totalItems;
    private int completedItems;
    private int inProgressItems;
    private int pendingItems;
    private int skippedItems;
    private double completedHours;
    private double remainingHours;
    private int remainingWeeks;
    private double currentRoleMatchPercentage;
    private double projectedRoleMatchPercentage;
    private double estimatedSkillCoverageGain;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getCompletedItems() {
        return completedItems;
    }

    public void setCompletedItems(int completedItems) {
        this.completedItems = completedItems;
    }

    public int getInProgressItems() {
        return inProgressItems;
    }

    public void setInProgressItems(int inProgressItems) {
        this.inProgressItems = inProgressItems;
    }

    public int getPendingItems() {
        return pendingItems;
    }

    public void setPendingItems(int pendingItems) {
        this.pendingItems = pendingItems;
    }

    public int getSkippedItems() {
        return skippedItems;
    }

    public void setSkippedItems(int skippedItems) {
        this.skippedItems = skippedItems;
    }

    public double getCompletedHours() {
        return completedHours;
    }

    public void setCompletedHours(double completedHours) {
        this.completedHours = completedHours;
    }

    public double getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(double remainingHours) {
        this.remainingHours = remainingHours;
    }

    public int getRemainingWeeks() {
        return remainingWeeks;
    }

    public void setRemainingWeeks(int remainingWeeks) {
        this.remainingWeeks = remainingWeeks;
    }

    public double getCurrentRoleMatchPercentage() {
        return currentRoleMatchPercentage;
    }

    public void setCurrentRoleMatchPercentage(double currentRoleMatchPercentage) {
        this.currentRoleMatchPercentage = currentRoleMatchPercentage;
    }

    public double getProjectedRoleMatchPercentage() {
        return projectedRoleMatchPercentage;
    }

    public void setProjectedRoleMatchPercentage(double projectedRoleMatchPercentage) {
        this.projectedRoleMatchPercentage = projectedRoleMatchPercentage;
    }

    public double getEstimatedSkillCoverageGain() {
        return estimatedSkillCoverageGain;
    }

    public void setEstimatedSkillCoverageGain(double estimatedSkillCoverageGain) {
        this.estimatedSkillCoverageGain = estimatedSkillCoverageGain;
    }
}
