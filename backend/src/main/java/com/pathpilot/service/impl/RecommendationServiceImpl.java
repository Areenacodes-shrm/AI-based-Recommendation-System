package com.pathpilot.service.impl;

import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.model.RecommendationItem;
import com.pathpilot.model.RecommendationResult;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.recommendation.strategy.HybridRecommendationStrategy;
import com.pathpilot.repository.RecommendationRepository;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentRepository studentRepository;
    private final HybridRecommendationStrategy hybridStrategy;
    private final RecommendationRepository recommendationRepository;

    public RecommendationServiceImpl(StudentRepository studentRepository,
                                     HybridRecommendationStrategy hybridStrategy,
                                     RecommendationRepository recommendationRepository) {
        this.studentRepository = studentRepository;
        this.hybridStrategy = hybridStrategy;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public RecommendationResult getRecommendationsForStudent(Long studentId, int limit) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        return hybridStrategy.recommend(student, limit);
    }

    @Override
    public List<RecommendationItem> getLatestRecommendations(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        return recommendationRepository.findLatestByStudentId(studentId);
    }
}
