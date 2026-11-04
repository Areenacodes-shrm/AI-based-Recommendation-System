package com.pathpilot.service;

import com.pathpilot.dto.FeedbackRequestDto;
import com.pathpilot.model.UserFeedback;

import java.util.List;

public interface FeedbackService {
    UserFeedback recordFeedback(Long studentId, FeedbackRequestDto dto);
    List<UserFeedback> getFeedbackForStudent(Long studentId);
}
