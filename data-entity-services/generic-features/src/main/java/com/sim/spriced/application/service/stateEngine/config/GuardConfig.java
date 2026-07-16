package com.sim.spriced.application.service.stateEngine.config;

public class GuardConfig {
    private String ruleName;
    private String onViolation;   // REJECT or ALTERNATE_TRANSITION
    private String alternateTransition;  // command name

    // getters & setters
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getOnViolation() { return onViolation; }
    public void setOnViolation(String onViolation) { this.onViolation = onViolation; }

    public String getAlternateTransition() { return alternateTransition; }
    public void setAlternateTransition(String alternateTransition) { this.alternateTransition = alternateTransition; }
}