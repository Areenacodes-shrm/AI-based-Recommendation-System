package com.pathpilot.service.impl;

import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.dto.FeedbackRequestDto;
import com.pathpilot.exception.InvalidRecommendationRequestException;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.FeedbackAction;
import com.pathpilot.model.UserFeedback;
import com.pathpilot.repository.FeedbackRepository;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final FeedbackTelemetryAggregator telemetryAggregator;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                               StudentRepository studentRepository,
                               OpportunityRepository opportunityRepository,
                               FeedbackTelemetryAggregator telemetryAggregator) {
        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
        this.telemetryAggregator = telemetryAggregator;
    }

    @Override
    @Transactional
    public UserFeedback recordFeedback(Long studentId, FeedbackRequestDto dto) {
        if (dto.getActionType() == FeedbackAction.RATED) {
            if (dto.getRatingValue() == null || dto.getRatingValue() < 1 || dto.getRatingValue() > 5) {
                throw new InvalidRecommendationRequestException("Rating value between 1 and 5 is required for RATED feedback.");
            }
        }

        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", dto.getOpportunityId()));

        if (dto.getActionType() == FeedbackAction.SAVED) {
            Optional<UserFeedback> existing = feedbackRepository.findByStudentOpportunityAndAction(
                    studentId, dto.getOpportunityId(), FeedbackAction.SAVED);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        UserFeedback fb = new UserFeedback(
                null,
                studentId,
                dto.getOpportunityId(),
                dto.getActionType(),
                dto.getRatingValue(),
                dto.getNotes()
        );

        UserFeedback saved = feedbackRepository.save(fb);
        telemetryAggregator.recordInteraction(dto.getOpportunityId(), dto.getActionType(), dto.getRatingValue());

        return saved;
    }

    @Override
    public List<UserFeedback> getFeedbackForStudent(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        return feedbackRepository.findByStudentId(studentId);
    }
}
