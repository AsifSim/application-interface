package com.sim.spriced.application.service.stateEngine;

import java.util.Map;

/**
 * Interface for evaluating business rule conditions and executing their actions.
 * This will be implemented by the generated rule engine that supports Excel-like functions.
 */
public interface RuleEngine {
    /**
     * Evaluate a named rule's condition within the given context.
     * @return true if the condition passes.
     */
    boolean evaluateCondition(String ruleName, Map<String, Object> context);

    /**
     * Execute all actions (or else-actions) of a named rule.
     */
    void executeActions(String ruleName, Map<String, Object> context);
}