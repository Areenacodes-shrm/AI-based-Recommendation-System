package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.AuthSessionResponseDto;
import com.pathpilot.dto.DemoLoginRequestDto;
import com.pathpilot.dto.DemoProfileDto;
import com.pathpilot.dto.LoginRequestDto;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthSessionService authSessionService;

    public AuthController(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthSessionResponseDto>> getCurrentSession(HttpServletRequest request) {
        Optional<StudentProfile> current = authSessionService.getCurrentStudent(request);
        return ResponseEntity.ok(ApiResponse.success(
                new AuthSessionResponseDto(current.isPresent(), current.orElse(null))
        ));
    }

    @GetMapping("/demo-profiles")
    public ResponseEntity<ApiResponse<List<DemoProfileDto>>> listDemoProfiles() {
        return ResponseEntity.ok(ApiResponse.success(authSessionService.getDemoProfiles()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthSessionResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto,
            HttpServletRequest request) {
        StudentProfile student = authSessionService.login(dto.getEmail(), dto.getPassword(), request);
        return ResponseEntity.ok(ApiResponse.success(
                new AuthSessionResponseDto(true, student),
                "Signed in successfully"
        ));
    }

    @PostMapping("/demo-login")
    public ResponseEntity<ApiResponse<AuthSessionResponseDto>> demoLogin(
            @Valid @RequestBody DemoLoginRequestDto dto,
            HttpServletRequest request) {
        StudentProfile student = authSessionService.demoLogin(dto.getStudentId(), request);
        return ResponseEntity.ok(ApiResponse.success(
                new AuthSessionResponseDto(true, student),
                "Switched to demo profile: " + student.getFullName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authSessionService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Signed out cleanly"));
    }
}
