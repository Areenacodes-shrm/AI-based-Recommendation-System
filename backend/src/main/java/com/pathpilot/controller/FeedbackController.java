package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.FeedbackRequestDto;
import com.pathpilot.model.UserFeedback;
import com.pathpilot.service.AuthSessionService;
import com.pathpilot.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AuthSessionService authSessionService;

    public FeedbackController(FeedbackService feedbackService, AuthSessionService authSessionService) {
        this.feedbackService = feedbackService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<UserFeedback>> submitFeedback(
            @PathVariable Long studentId,
            @Valid @RequestBody FeedbackRequestDto dto,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        UserFeedback fb = feedbackService.recordFeedback(studentId, dto);
        return ResponseEntity.ok(ApiResponse.success(fb, "Feedback recorded successfully"));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getStudentFeedback(
            @PathVariable Long studentId,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, studentId);
        List<UserFeedback> feedbacks = feedbackService.getFeedbackForStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }
}
