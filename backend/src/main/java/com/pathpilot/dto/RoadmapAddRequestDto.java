package com.pathpilot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RoadmapAddRequestDto {

    @NotNull(message = "Opportunity ID is required")
    private Long opportunityId;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;

    public RoadmapAddRequestDto() {}

    public RoadmapAddRequestDto(Long opportunityId, String note) {
        this.opportunityId = opportunityId;
        this.note = note;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
