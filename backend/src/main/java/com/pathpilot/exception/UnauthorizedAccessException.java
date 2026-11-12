package com.pathpilot.exception;

/**
 * Thrown when an authenticated user attempts to access or modify another student's private resource (HTTP 403).
 */
public class UnauthorizedAccessException extends PathPilotException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
