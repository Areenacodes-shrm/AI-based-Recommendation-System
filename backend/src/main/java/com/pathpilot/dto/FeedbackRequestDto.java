package com.pathpilot.dto;

import com.pathpilot.model.FeedbackAction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FeedbackRequestDto {

    @NotNull(message = "Opportunity ID is required")
    private Long opportunityId;

    @NotNull(message = "Feedback action type is required")
    private FeedbackAction actionType;

    @Min(value = 1, message = "Rating value must be between 1 and 5")
    @Max(value = 5, message = "Rating value must be between 1 and 5")
    private Integer ratingValue;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
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
