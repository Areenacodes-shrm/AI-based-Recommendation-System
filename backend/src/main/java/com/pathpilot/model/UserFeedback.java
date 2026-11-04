package com.pathpilot.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates explicit student feedback and interactions on recommendations.
 */
public class UserFeedback {
    private Long id;
    private Long studentId;
    private Long opportunityId;
    private FeedbackAction actionType;
    private Integer ratingValue; // 1 to 5
    private String notes;
    private LocalDateTime createdAt;

    public UserFeedback() {}

    public UserFeedback(Long id, Long studentId, Long opportunityId, FeedbackAction actionType, Integer ratingValue, String notes) {
        this.id = id;
        this.studentId = Objects.requireNonNull(studentId, "Student ID cannot be null");
        this.opportunityId = Objects.requireNonNull(opportunityId, "Opportunity ID cannot be null");
        this.actionType = Objects.requireNonNull(actionType, "Action type cannot be null");
        if (ratingValue != null) {
            this.ratingValue = Math.max(1, Math.min(5, ratingValue));
        }
        this.notes = notes;
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

    public FeedbackAction getActionType() {
        return actionType;
    }

    public void setActionType(FeedbackAction actionType) {
        this.actionType = actionType;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = (ratingValue != null) ? Math.max(1, Math.min(5, ratingValue)) : null;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
