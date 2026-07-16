package com.sim.spriced.application.service.stateEngine;

import com.sim.spriced.application.service.autosegmentation.stateEngine.config.GuardConfig;
import com.sim.spriced.application.service.autosegmentation.stateEngine.config.StateMachineConfig;
import com.sim.spriced.application.service.autosegmentation.stateEngine.config.TransitionConfig;
import com.sim.spriced.application.service.autosegmentation.stateEngine.exception.*;
import java.util.*;
import java.util.stream.Collectors;


public class StateMachineEngine {
    private final Map<String, Map<String, String>> transitions = new HashMap<>();  // fromState -> (command -> toState)
    private final Map<String, List<Guard>> guards = new HashMap<>();               // command -> list of guards
    private final String initialState;
    private final Map<String, List<String>> entryActions;   // state -> list of rule names
    private final Map<String, List<String>> exitActions;
    private final RuleEngine ruleEngine;

    public StateMachineEngine(StateMachineConfig config, RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
        this.initialState = config.getInitialState();
        this.entryActions = config.getEntryActions();
        this.exitActions = config.getExitActions();
        buildTransitions(config);
    }

    private void buildTransitions(StateMachineConfig config) {
        for (TransitionConfig tc : config.getTransitions()) {
            // Use null to represent wildcard "*" in the map key.
            String from = tc.getFrom().equals("*") ? null : tc.getFrom();
            String to = tc.getTo();
            transitions.computeIfAbsent(from, k -> new HashMap<>()).put(tc.getCommand(), to);
            if (tc.getGuards() != null && !tc.getGuards().isEmpty()) {
                List<Guard> cmdGuards = tc.getGuards().stream()
                        .map(gc -> buildGuard(gc))
                        .collect(Collectors.toList());
                guards.computeIfAbsent(tc.getCommand(), k -> new ArrayList<>()).addAll(cmdGuards);
            }
        }
    }

    private Guard buildGuard(GuardConfig gc) {
        return (currentState, context) -> {
            boolean ruleResult = ruleEngine.evaluateCondition(gc.getRuleName(), context);
            if (!ruleResult) {
                if ("REJECT".equals(gc.getOnViolation())) {
                    throw new GuardViolationException(gc.getRuleName());
                } else if ("ALTERNATE_TRANSITION".equals(gc.getOnViolation())) {
                    throw new AlternateTransitionException(gc.getAlternateTransition());
                }
            }
        };
    }

    public String getInitialState() { return initialState; }

    /**
     * Fire a command on a record with the given currentState.
     * Returns the new state after executing exit/entry rules.
     */
    public String fire(String currentState, String command, Map<String, Object> context) {
        // 1. Look up transition
        Map<String, String> eventMap = transitions.get(currentState);
        if (eventMap == null) {
            eventMap = transitions.get(null); // wildcard
        }
        if (eventMap == null || !eventMap.containsKey(command)) {
            throw new InvalidStateTransitionException(
                    String.format("No transition from '%s' with command '%s'", currentState, command));
        }
        String targetState = eventMap.get(command);

        // 2. Evaluate guards – may throw GuardViolationException or AlternateTransitionException
        List<Guard> cmdGuards = guards.getOrDefault(command, Collections.emptyList());
        for (Guard guard : cmdGuards) {
            guard.evaluate(currentState, context);
        }

        // 3. Run exit actions of the current state
        executeRules(exitActions.getOrDefault(currentState, Collections.emptyList()), context);

        // 4. Run entry actions of the target state
        executeRules(entryActions.getOrDefault(targetState, Collections.emptyList()), context);

        return targetState;
    }

    private void executeRules(List<String> ruleNames, Map<String, Object> context) {
        for (String ruleName : ruleNames) {
            ruleEngine.executeActions(ruleName, context);
        }
    }
}