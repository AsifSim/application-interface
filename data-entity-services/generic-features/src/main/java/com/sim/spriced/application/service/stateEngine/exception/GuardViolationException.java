package com.sim.spriced.application.service.stateEngine.exception;

public class GuardViolationException extends RuntimeException {
    public GuardViolationException(String ruleName) {
        super("Guard violation: " + ruleName);
    }
}
