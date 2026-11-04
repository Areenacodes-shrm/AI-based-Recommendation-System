package com.pathpilot.service;

import com.pathpilot.dto.StudentProfileUpdateDto;
import com.pathpilot.dto.StudentRegistrationDto;
import com.pathpilot.model.StudentProfile;

import java.util.List;

public interface StudentService {
    StudentProfile registerStudent(StudentRegistrationDto dto);
    StudentProfile getStudentById(Long id);
    StudentProfile getStudentByEmail(String email);
    StudentProfile updateStudentProfile(Long id, StudentProfileUpdateDto dto);
    List<StudentProfile> getAllStudents();
}
