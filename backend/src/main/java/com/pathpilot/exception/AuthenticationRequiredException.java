package com.pathpilot.exception;

/**
 * Thrown when an operation requires an active authenticated student session (HTTP 401).
 */
public class AuthenticationRequiredException extends PathPilotException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
