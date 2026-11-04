package com.pathpilot.dto;

import com.pathpilot.model.PlanStatus;

public class LearningPlanUpdateDto {
    private PlanStatus status;
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
