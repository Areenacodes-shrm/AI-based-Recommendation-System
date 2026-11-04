package com.pathpilot.dto;

import com.pathpilot.model.FeedbackAction;

public class FeedbackRequestDto {
    private Long opportunityId;
    private FeedbackAction actionType;
    private Integer ratingValue;
    private String notes;

    public FeedbackRequestDto() {}

    public FeedbackRequestDto(Long opportunityId, FeedbackAction actionType, Integer ratingValue, String notes) {
        this.opportunityId = opportunityId;
        this.actionType = actionType;
        this.ratingValue = ratingValue;
        this.notes = notes;
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
        this.ratingValue = ratingValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
