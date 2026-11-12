package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.StudentProfileUpdateDto;
import com.pathpilot.dto.StudentRegistrationDto;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.service.AuthSessionService;
import com.pathpilot.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final AuthSessionService authSessionService;

    public StudentController(StudentService studentService, AuthSessionService authSessionService) {
        this.studentService = studentService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StudentProfile>> register(
            @Valid @RequestBody StudentRegistrationDto dto,
            HttpServletRequest request) {
        StudentProfile created = studentService.registerStudent(dto);
        authSessionService.establishAuthenticatedSession(request, created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Student registered successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfile>> getById(@PathVariable Long id, HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, id);
        StudentProfile student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<StudentProfile>> getByEmail(@PathVariable String email, HttpServletRequest request) {
        Long activeId = authSessionService.requireAuthenticatedStudentId(request);
        StudentProfile student = studentService.getStudentByEmail(email);
        authSessionService.requireAuthorizedStudent(request, student.getId());
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfile>> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentProfileUpdateDto dto,
            HttpServletRequest request) {
        authSessionService.requireAuthorizedStudent(request, id);
        StudentProfile updated = studentService.updateStudentProfile(id, dto);
        authSessionService.establishAuthenticatedSession(request, updated);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentProfile>>> getOwnProfileList(HttpServletRequest request) {
        Long activeId = authSessionService.requireAuthenticatedStudentId(request);
        StudentProfile student = studentService.getStudentById(activeId);
        return ResponseEntity.ok(ApiResponse.success(List.of(student)));
    }
}
