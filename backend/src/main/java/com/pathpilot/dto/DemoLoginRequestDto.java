package com.pathpilot.dto;

import jakarta.validation.constraints.NotNull;

public class DemoLoginRequestDto {

    @NotNull(message = "Demo student ID is required")
    private Long studentId;

    public DemoLoginRequestDto() {}

    public DemoLoginRequestDto(Long studentId) {
        this.studentId = studentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
