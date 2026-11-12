package com.pathpilot.repository;

import com.pathpilot.model.FeedbackAction;
import com.pathpilot.model.UserFeedback;

import java.util.List;
import java.util.Optional;

/**
 * Interface contract for User Feedback persistence operations.
 */
public interface FeedbackRepository {
    UserFeedback save(UserFeedback feedback);
    List<UserFeedback> findByStudentId(Long studentId);
    List<UserFeedback> findByOpportunityId(Long opportunityId);
    Optional<UserFeedback> findByStudentAndOpportunity(Long studentId, Long opportunityId);
    Optional<UserFeedback> findByStudentOpportunityAndAction(Long studentId, Long opportunityId, FeedbackAction actionType);
    List<UserFeedback> findAll();
}
