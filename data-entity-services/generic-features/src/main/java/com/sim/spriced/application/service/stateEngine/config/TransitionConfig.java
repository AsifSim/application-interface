package com.sim.spriced.application.service.stateEngine.config;

import java.util.List;

public class TransitionConfig {
    private String from;          // state name or "*"
    private String to;
    private String command;
    private String event;
    private List<GuardConfig> guards;

    // getters & setters
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public List<GuardConfig> getGuards() { return guards; }
    public void setGuards(List<GuardConfig> guards) { this.guards = guards; }
}