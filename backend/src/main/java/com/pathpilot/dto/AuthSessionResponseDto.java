package com.pathpilot.dto;

import com.pathpilot.model.StudentProfile;

public record AuthSessionResponseDto(boolean authenticated, StudentProfile student) {}
