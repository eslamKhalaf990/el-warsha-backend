package com.warsha.erp.controllers;

import com.warsha.erp.exceptions.InsufficientStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleStockException(InsufficientStockException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Stock Error");
        response.put("message", ex.getMessage()); // This will show "Insufficient stock for: اسوره ايه الكرسي اسود"
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}