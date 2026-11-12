package com.pathpilot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathpilot.dto.LoginRequestDto;
import com.pathpilot.dto.StudentRegistrationDto;
import com.pathpilot.model.DifficultyLevel;
import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.model.StudentProfile;
import com.pathpilot.service.RoadmapService;
import com.pathpilot.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Session Auth, Ownership Access Control & Bean Validation Integration Tests")
class SecurityAndControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentService studentService;

    @Autowired
    private RoadmapService roadmapService;

    @Test
    @DisplayName("Private student endpoints must reject unauthenticated requests with 401")
    void shouldRejectUnauthenticatedPrivateRequests() throws Exception {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");

        mockMvc.perform(get("/api/students/" + alex.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/recommendations/student/" + alex.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/roadmap/student/" + alex.getId() + "/export"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/skill-gap/student/" + alex.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Signed-in student must be blocked from accessing or mutating another student's data (403)")
    void shouldEnforceOwnershipAcrossUsers() throws Exception {
        StudentProfile alex = studentService.getStudentByEmail("alex.rivera@university.edu");
        StudentProfile priya = studentService.getStudentByEmail("priya.sharma@tech.edu");
        List<LearningPlanItem> priyaRoadmap = roadmapService.getRoadmap(priya.getId());
        assertFalse(priyaRoadmap.isEmpty());

        MockHttpSession initialSession = new MockHttpSession();
        String initialSessionId = initialSession.getId();
        MvcResult loginResult = mockMvc.perform(post("/api/auth/demo-login")
                        .session(initialSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("studentId", alex.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andReturn();

        MockHttpSession alexSession = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(alexSession);
        assertNotEquals(initialSessionId, alexSession.getId(), "Session ID must rotate on authentication");

        // Own profile succeeds
        mockMvc.perform(get("/api/students/" + alex.getId()).session(alexSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        // Cross-user read & mutation attempts fail with 403
        mockMvc.perform(get("/api/students/" + priya.getId()).session(alexSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/recommendations/student/" + priya.getId()).session(alexSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/roadmap/student/" + priya.getId() + "/export").session(alexSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/roadmap/item/" + priyaRoadmap.get(0).getId()).session(alexSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Registration, password login, demo-login restriction, and Bean Validation should work cleanly")
    void shouldHandleRegistrationPasswordAuthAndValidation() throws Exception {
        // Invalid registration triggers 400 validation response
        StudentRegistrationDto invalidDto = new StudentRegistrationDto();
        invalidDto.setFullName("A");
        invalidDto.setEmail("not-an-email");
        invalidDto.setPassword("short");
        invalidDto.setTargetRole("");

        mockMvc.perform(post("/api/students/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Valid registration establishes session and never returns passwordHash
        StudentRegistrationDto validDto = new StudentRegistrationDto();
        validDto.setFullName("Maya Lin");
        validDto.setEmail("maya.lin@university.edu");
        validDto.setPassword("SecurePass#2026");
        validDto.setTargetRole("Software Engineer");
        validDto.setWeeklyHoursAvailable(16);
        validDto.setMaxBudget(BigDecimal.valueOf(80.00));
        validDto.setPreferredDifficulty(DifficultyLevel.INTERMEDIATE);

        MockHttpSession initialSession = new MockHttpSession();
        MvcResult regRes = mockMvc.perform(post("/api/students/register")
                        .session(initialSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andReturn();

        MockHttpSession activeSession = (MockHttpSession) regRes.getRequest().getSession(false);
        assertNotNull(activeSession);

        String regBody = regRes.getResponse().getContentAsString();
        assertFalse(regBody.contains("passwordHash"));
        assertFalse(regBody.contains("SecurePass#2026"));
        JsonNode createdNode = objectMapper.readTree(regBody).get("data");
        long mayaId = createdNode.get("id").asLong();

        // Newly registered student has authenticated session access immediately
        mockMvc.perform(get("/api/students/" + mayaId).session(activeSession))
                .andExpect(status().isOk());

        // Non-demo student cannot be impersonated via demo-login
        mockMvc.perform(post("/api/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("studentId", mayaId))))
                .andExpect(status().isForbidden());

        // Logout invalidates session so subsequent access fails with 401
        mockMvc.perform(post("/api/auth/logout").session(activeSession))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/students/" + mayaId).session(activeSession))
                .andExpect(status().isUnauthorized());

        // Wrong password returns 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("maya.lin@university.edu", "WrongPass"))))
                .andExpect(status().isUnauthorized());

        // Correct password starts a fresh authenticated session
        MvcResult reloginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto("maya.lin@university.edu", "SecurePass#2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.student.id").value(mayaId))
                .andReturn();

        MockHttpSession reloginSession = (MockHttpSession) reloginRes.getRequest().getSession(false);
        assertNotNull(reloginSession);
        mockMvc.perform(get("/api/students/" + mayaId).session(reloginSession))
                .andExpect(status().isOk());
    }
}
