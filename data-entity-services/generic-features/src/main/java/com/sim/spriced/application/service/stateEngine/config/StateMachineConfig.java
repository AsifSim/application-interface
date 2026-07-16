package com.sim.spriced.application.service.stateEngine.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachineConfig {
    private String entity;
    private String stateColumn;
    private String initialState;
    private List<String> states;
    private List<TransitionConfig> transitions;
    private Map<String, List<String>> entryActions = new HashMap<>();
    private Map<String, List<String>> exitActions  = new HashMap<>();

    // getters & setters (lombok @Data would be fine)
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public String getStateColumn() { return stateColumn; }
    public void setStateColumn(String stateColumn) { this.stateColumn = stateColumn; }

    public String getInitialState() { return initialState; }
    public void setInitialState(String initialState) { this.initialState = initialState; }

    public List<String> getStates() { return states; }
    public void setStates(List<String> states) { this.states = states; }

    public List<TransitionConfig> getTransitions() { return transitions; }
    public void setTransitions(List<TransitionConfig> transitions) { this.transitions = transitions; }

    public Map<String, List<String>> getEntryActions() { return entryActions; }
    public void setEntryActions(Map<String, List<String>> entryActions) { this.entryActions = entryActions; }

    public Map<String, List<String>> getExitActions() { return exitActions; }
    public void setExitActions(Map<String, List<String>> exitActions) { this.exitActions = exitActions; }
}