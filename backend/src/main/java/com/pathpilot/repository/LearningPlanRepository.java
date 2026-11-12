package com.pathpilot.repository;

import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.model.PlanStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interface contract for Learning Plan Roadmap persistence operations.
 */
public interface LearningPlanRepository {
    LearningPlanItem save(LearningPlanItem item);
    List<LearningPlanItem> findByStudentId(Long studentId);
    Optional<LearningPlanItem> findById(Long id);
    Optional<LearningPlanItem> findByStudentIdAndOpportunityId(Long studentId, Long opportunityId);
    void updateStatus(Long id, PlanStatus status, Integer targetCompletionWeeks);
    boolean deleteById(Long id);
    void clearForStudent(Long studentId);
}
