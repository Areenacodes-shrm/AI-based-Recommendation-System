package com.pathpilot.service;

import com.pathpilot.dto.DemoProfileDto;
import com.pathpilot.exception.AuthenticationRequiredException;
import com.pathpilot.exception.ResourceNotFoundException;
import com.pathpilot.exception.UnauthorizedAccessException;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.repository.StudentRepository;
import com.pathpilot.util.PasswordHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Coordinates Jakarta HttpSession authentication and student resource authorization.
 * Session state stores only minimal non-sensitive identifiers; passwords and secrets are never placed in sessions.
 */
@Service
public class AuthSessionService {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionService.class);

    public static final String SESSION_STUDENT_ID = "PATHPILOT_ACTIVE_STUDENT_ID";
    public static final String SESSION_STUDENT_NAME = "PATHPILOT_ACTIVE_STUDENT_NAME";
    public static final String SESSION_AUTHENTICATED = "PATHPILOT_IS_AUTHENTICATED";
    public static final String SESSION_STATUS_CHECKS = "STATUS_CHECKS_COUNT";
    public static final String SESSION_LAST_STATUS_CHECK = "LAST_STATUS_CHECK";

    private final StudentRepository studentRepository;

    public AuthSessionService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<DemoProfileDto> getDemoProfiles() {
        return studentRepository.findDemoProfiles();
    }

    public StudentProfile login(String email, String rawPassword, HttpServletRequest request) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new AuthenticationRequiredException("Invalid email or password.");
        }

        StudentProfile student = studentRepository.findByEmail(email.trim())
                .orElseThrow(() -> new AuthenticationRequiredException("Invalid email or password."));

        if (!PasswordHasher.matches(rawPassword, student.getPasswordHash())) {
            throw new AuthenticationRequiredException("Invalid email or password.");
        }

        establishAuthenticatedSession(request, student);
        log.info("Authenticated session started for student id={}", student.getId());
        return student;
    }

    public StudentProfile demoLogin(Long studentId, HttpServletRequest request) {
        if (studentId == null) {
            throw new AuthenticationRequiredException("A demo student ID is required.");
        }

        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        if (!student.isDemoAccount()) {
            throw new UnauthorizedAccessException("Quick demo login is only allowed for built-in demo personas.");
        }

        establishAuthenticatedSession(request, student);
        log.info("Demo session switched to demo student id={}", student.getId());
        return student;
    }

    public void establishAuthenticatedSession(HttpServletRequest request, StudentProfile student) {
        HttpSession existing = request.getSession(false);
        Integer priorStatusChecks = null;
        String priorLastCheck = null;
        if (existing != null) {
            Object checksAttr = existing.getAttribute(SESSION_STATUS_CHECKS);
            if (checksAttr instanceof Integer count) {
                priorStatusChecks = count;
            }
            Object lastAttr = existing.getAttribute(SESSION_LAST_STATUS_CHECK);
            if (lastAttr instanceof String ts) {
                priorLastCheck = ts;
            }
            // Rotate session identifier on authentication state changes to mitigate session fixation
            existing.invalidate();
        }

        HttpSession fresh = request.getSession(true);
        fresh.setAttribute(SESSION_STUDENT_ID, student.getId());
        fresh.setAttribute(SESSION_STUDENT_NAME, student.getFullName());
        fresh.setAttribute(SESSION_AUTHENTICATED, Boolean.TRUE);
        if (priorStatusChecks != null) {
            fresh.setAttribute(SESSION_STATUS_CHECKS, priorStatusChecks);
        }
        if (priorLastCheck != null) {
            fresh.setAttribute(SESSION_LAST_STATUS_CHECK, priorLastCheck);
        }
    }

    public void logout(HttpServletRequest request) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
    }

    public Optional<StudentProfile> getCurrentStudent(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute(SESSION_AUTHENTICATED))) {
            return Optional.empty();
        }
        Object idAttr = session.getAttribute(SESSION_STUDENT_ID);
        if (!(idAttr instanceof Long studentId)) {
            session.invalidate();
            return Optional.empty();
        }

        Optional<StudentProfile> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            session.invalidate();
        }
        return student;
    }

    public Long requireAuthenticatedStudentId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute(SESSION_AUTHENTICATED))) {
            throw new AuthenticationRequiredException("Please sign in or select a demo student profile to continue.");
        }
        Object idAttr = session.getAttribute(SESSION_STUDENT_ID);
        if (!(idAttr instanceof Long studentId)) {
            throw new AuthenticationRequiredException("Active session is invalid or expired. Please sign in again.");
        }
        return studentId;
    }

    public void requireAuthorizedStudent(HttpServletRequest request, Long requestedStudentId) {
        Long sessionStudentId = requireAuthenticatedStudentId(request);
        if (requestedStudentId == null || !sessionStudentId.equals(requestedStudentId)) {
            throw new UnauthorizedAccessException("Access denied: you may only access or modify your own student profile and progress.");
        }
    }
}
