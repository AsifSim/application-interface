package com.sim.spriced.application.service.stateEngine;

import com.sim.spriced.application.service.autosegmentation.stateEngine.exception.AlternateTransitionException;
import com.sim.spriced.application.service.autosegmentation.stateEngine.exception.GuardViolationException;

import java.util.Map;

/**
 * Evaluates a guard condition. Implementations will call RuleEngine.
 * Context is a map holding all current data (request, enrichment, computed values).
 */
@FunctionalInterface
public interface Guard {
    void evaluate(String currentState, Map<String, Object> context)
            throws GuardViolationException, AlternateTransitionException;
}