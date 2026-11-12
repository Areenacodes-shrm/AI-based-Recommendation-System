package com.pathpilot.dto;

import com.pathpilot.model.PlanStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class LearningPlanUpdateDto {
    private PlanStatus status;

    @Min(value = 1, message = "Target completion weeks must be at least 1")
    @Max(value = 52, message = "Target completion weeks cannot exceed 52")
    private Integer targetCompletionWeeks;

    public LearningPlanUpdateDto() {}

    public LearningPlanUpdateDto(PlanStatus status, Integer targetCompletionWeeks) {
        this.status = status;
        this.targetCompletionWeeks = targetCompletionWeeks;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public Integer getTargetCompletionWeeks() {
        return targetCompletionWeeks;
    }

    public void setTargetCompletionWeeks(Integer targetCompletionWeeks) {
        this.targetCompletionWeeks = targetCompletionWeeks;
    }
}
