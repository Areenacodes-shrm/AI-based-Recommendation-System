package com.pathpilot.exception;

/**
 * Thrown when recommendation parameters, constraints, or student profiles are invalid.
 */
public class InvalidRecommendationRequestException extends PathPilotException {
    public InvalidRecommendationRequestException(String message) {
        super(message);
    }
}
