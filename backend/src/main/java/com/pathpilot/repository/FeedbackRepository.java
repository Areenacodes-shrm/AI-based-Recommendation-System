package com.pathpilot.repository;

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
    List<UserFeedback> findAll();
}
