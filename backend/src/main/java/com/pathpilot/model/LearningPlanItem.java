package com.pathpilot.model;

import com.pathpilot.model.opportunity.Opportunity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulated domain model representing a structured step in the student's learning roadmap.
 */
public class LearningPlanItem {
    private Long id;
    private Long studentId;
    private Long opportunityId;
    private int sequenceOrder;
    private PlanStatus status;
    private int targetCompletionWeeks;
    private String prerequisiteNote;
    private Opportunity opportunity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LearningPlanItem() {
        this.status = PlanStatus.PENDING;
        this.sequenceOrder = 1;
        this.targetCompletionWeeks = 4;
    }

    public LearningPlanItem(Long id, Long studentId, Long opportunityId, int sequenceOrder,
                            PlanStatus status, int targetCompletionWeeks, String prerequisiteNote) {
        this.id = id;
        this.studentId = Objects.requireNonNull(studentId, "Student ID cannot be null");
        this.opportunityId = Objects.requireNonNull(opportunityId, "Opportunity ID cannot be null");
        this.sequenceOrder = sequenceOrder;
        this.status = status != null ? status : PlanStatus.PENDING;
        this.targetCompletionWeeks = Math.max(1, targetCompletionWeeks);
        this.prerequisiteNote = prerequisiteNote;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public int getTargetCompletionWeeks() {
        return targetCompletionWeeks;
    }

    public void setTargetCompletionWeeks(int targetCompletionWeeks) {
        this.targetCompletionWeeks = Math.max(1, targetCompletionWeeks);
    }

    public String getPrerequisiteNote() {
        return prerequisiteNote;
    }

    public void setPrerequisiteNote(String prerequisiteNote) {
        this.prerequisiteNote = prerequisiteNote;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
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
}
