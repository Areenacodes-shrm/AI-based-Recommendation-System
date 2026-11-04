package com.pathpilot.service.impl;

import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.dto.FeedbackRequestDto;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.UserFeedback;
import com.pathpilot.repository.FeedbackRepository;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity", dto.getOpportunityId()));

        UserFeedback fb = new UserFeedback(
                null,
                studentId,
                dto.getOpportunityId(),
                dto.getActionType(),
                dto.getRatingValue(),
                dto.getNotes()
        );

        UserFeedback saved = feedbackRepository.save(fb);

        // Update real-time concurrent telemetry aggregator
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
