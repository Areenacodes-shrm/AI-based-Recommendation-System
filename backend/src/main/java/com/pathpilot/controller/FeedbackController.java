package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.FeedbackRequestDto;
import com.pathpilot.model.UserFeedback;
import com.pathpilot.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<UserFeedback>> submitFeedback(
            @PathVariable Long studentId,
            @RequestBody FeedbackRequestDto dto) {
        UserFeedback fb = feedbackService.recordFeedback(studentId, dto);
        return ResponseEntity.ok(ApiResponse.success(fb, "Feedback recorded successfully"));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<UserFeedback>>> getStudentFeedback(@PathVariable Long studentId) {
        List<UserFeedback> feedbacks = feedbackService.getFeedbackForStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }
}
