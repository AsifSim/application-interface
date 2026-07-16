package com.sim.spriced.application.service.stateEngine.exception;

// Exceptions
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}