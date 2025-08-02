package com.logistics.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for consistent error responses across the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle invalid order exceptions
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrder(InvalidOrderException ex, WebRequest request) {
        logger.warn("Invalid order exception: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getOrderField() != null) {
            details.put("field", ex.getOrderField());
            details.put("rejectedValue", ex.getRejectedValue());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Order",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle order not found exceptions
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        logger.warn("Order not found: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getOrderId() != null) {
            details.put("orderId", ex.getOrderId());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Order Not Found",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle insufficient inventory exceptions
     */
    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientInventory(InsufficientInventoryException ex, WebRequest request) {
        logger.warn("Insufficient inventory: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getProductSku() != null) {
            details.put("productSku", ex.getProductSku());
            details.put("availableQuantity", ex.getAvailableQuantity());
            details.put("requestedQuantity", ex.getRequestedQuantity());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Insufficient Inventory",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle order processing exceptions
     */
    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<ErrorResponse> handleOrderProcessing(OrderProcessingException ex, WebRequest request) {
        logger.error("Order processing failed: {}", ex.getMessage(), ex);
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getOrderId() != null) {
            details.put("orderId", ex.getOrderId());
        }
        if (ex.getProcessingStage() != null) {
            details.put("processingStage", ex.getProcessingStage());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Order Processing Failed",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle product not found exceptions
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex, WebRequest request) {
        logger.warn("Product not found: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getSku() != null) {
            details.put("sku", ex.getSku());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Product Not Found",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle shipment exceptions
     */
    @ExceptionHandler(ShipmentException.class)
    public ResponseEntity<ErrorResponse> handleShipmentException(ShipmentException ex, WebRequest request) {
        logger.warn("Shipment operation failed: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getShipmentId() != null) {
            details.put("shipmentId", ex.getShipmentId());
            details.put("currentStatus", ex.getCurrentStatus());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Shipment Operation Failed",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle AI agent exceptions
     */
    @ExceptionHandler(AIAgentException.class)
    public ResponseEntity<ErrorResponse> handleAIAgentException(AIAgentException ex, WebRequest request) {
        logger.error("AI Agent failed: {}", ex.getMessage(), ex);
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getAgentName() != null) {
            details.put("agentName", ex.getAgentName());
            details.put("operation", ex.getOperation());
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "AI Agent Service Unavailable",
            ex.getMessage(),
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing
            ));
        
        details.put("fieldErrors", fieldErrors);
        details.put("rejectedFields", fieldErrors.keySet());
        
        String message = "Validation failed for " + fieldErrors.size() + " field(s)";
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            message,
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        logger.warn("Binding failed: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        Map<String, String> fieldErrors = ex.getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
            ));
        
        details.put("fieldErrors", fieldErrors);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Binding Failed",
            "Request parameter binding failed",
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        Map<String, String> violations = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        
        details.put("violations", violations);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Constraint Violation",
            "Request contains constraint violations",
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        logger.warn("Type mismatch: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getName());
        details.put("value", ex.getValue());
        details.put("requiredType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                                      ex.getValue(), ex.getName(), 
                                      ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Type Mismatch",
            message,
            getPath(request),
            details
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later or contact support.",
            getPath(request)
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Extract request path from WebRequest
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}