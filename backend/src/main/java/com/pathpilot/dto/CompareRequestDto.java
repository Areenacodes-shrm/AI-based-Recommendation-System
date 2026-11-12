package com.pathpilot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CompareRequestDto {

    @NotEmpty(message = "At least 2 opportunity IDs are required for comparison")
    @Size(min = 2, max = 3, message = "Comparison requires between 2 and 3 opportunities")
    private List<Long> opportunityIds;

    public CompareRequestDto() {}

    public CompareRequestDto(List<Long> opportunityIds) {
        this.opportunityIds = opportunityIds;
    }

    public List<Long> getOpportunityIds() {
        return opportunityIds;
    }

    public void setOpportunityIds(List<Long> opportunityIds) {
        this.opportunityIds = opportunityIds;
    }
}
