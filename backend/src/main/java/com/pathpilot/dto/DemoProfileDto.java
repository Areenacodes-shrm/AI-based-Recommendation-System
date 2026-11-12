package com.pathpilot.dto;

/**
 * Minimal non-sensitive public projection used for demo account switching prior to login.
 */
public record DemoProfileDto(Long id, String fullName, String targetRole) {}
