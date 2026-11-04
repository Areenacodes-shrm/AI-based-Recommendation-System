package com.pathpilot.controller;

import com.pathpilot.dto.ApiResponse;
import com.pathpilot.dto.StudentProfileUpdateDto;
import com.pathpilot.dto.StudentRegistrationDto;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StudentProfile>> register(@RequestBody StudentRegistrationDto dto) {
        StudentProfile created = studentService.registerStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Student registered successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfile>> getById(@PathVariable Long id) {
        StudentProfile student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<StudentProfile>> getByEmail(@PathVariable String email) {
        StudentProfile student = studentService.getStudentByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfile>> update(@PathVariable Long id, @RequestBody StudentProfileUpdateDto dto) {
        StudentProfile updated = studentService.updateStudentProfile(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentProfile>>> getAll() {
        List<StudentProfile> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }
}
