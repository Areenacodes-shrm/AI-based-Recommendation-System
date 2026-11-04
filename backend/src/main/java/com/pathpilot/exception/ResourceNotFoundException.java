package com.pathpilot.exception;

/**
 * Thrown when an entity (student, opportunity, skill, or learning plan) cannot be located.
 */
public class ResourceNotFoundException extends PathPilotException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s with identifier '%s' was not found.", resourceName, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
