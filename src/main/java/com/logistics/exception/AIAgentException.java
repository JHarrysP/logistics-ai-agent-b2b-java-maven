package com.logistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when AI agent operations fail
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AIAgentException extends RuntimeException {
    private final String agentName;
    private final String operation;
    
    public AIAgentException(String message) {
        super(message);
        this.agentName = null;
        this.operation = null;
    }
    
    public AIAgentException(String agentName, String operation, String message) {
        super(String.format("AI Agent '%s' failed during operation '%s': %s", 
              agentName, operation, message));
        this.agentName = agentName;
        this.operation = operation;
    }
    
    public AIAgentException(String agentName, String operation, String message, Throwable cause) {
        super(String.format("AI Agent '%s' failed during operation '%s': %s", 
              agentName, operation, message), cause);
        this.agentName = agentName;
        this.operation = operation;
    }
    
    public String getAgentName() { return agentName; }
    public String getOperation() { return operation; }
}