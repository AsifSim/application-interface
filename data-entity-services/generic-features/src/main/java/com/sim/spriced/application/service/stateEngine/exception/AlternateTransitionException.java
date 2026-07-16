package com.sim.spriced.application.service.stateEngine.exception;

public class AlternateTransitionException extends RuntimeException {
    private final String alternateCommand;

    public AlternateTransitionException(String alternateCommand) {
        super("Guard forced alternate transition: " + alternateCommand);
        this.alternateCommand = alternateCommand;
    }

    public String getAlternateCommand() {
        return alternateCommand;
    }
}