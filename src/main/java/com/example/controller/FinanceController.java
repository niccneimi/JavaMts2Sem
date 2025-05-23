package com.example.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/finance")
@RateLimiter(name = "financeService")
public class FinanceController {

    private final UserService userService;

    public FinanceController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processFinancialOperation(
            @RequestParam(required = false) String operationId,
            @RequestParam Long userId,
            @RequestParam Double amount) {
        
        log.info("Получен запрос на финансовую операцию: operationId={}, userId={}, amount={}", 
                operationId, userId, amount);
        
        try {
            boolean result = userService.processFinancialOperation(operationId, userId, amount);
            
            if (result) {
                return ResponseEntity.ok("Финансовая операция успешно выполнена");
            } else {
                return ResponseEntity
                    .status(422)
                    .body("Не удалось выполнить финансовую операцию");
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке финансовой операции: {}", e.getMessage());
            return ResponseEntity
                .status(500)
                .body("Произошла ошибка: " + e.getMessage());
        }
    }
    
    @PostMapping("/process/retry")
    public ResponseEntity<String> processFinancialOperationWithRetry(
            @RequestParam String operationId,
            @RequestParam Long userId,
            @RequestParam Double amount) {
        
        log.info("Получен повторный запрос на финансовую операцию с ID: {}", operationId);
        boolean result = userService.processFinancialOperation(operationId, userId, amount);
        
        if (result) {
            return ResponseEntity.ok("Финансовая операция выполнена успешно");
        } else {
            return ResponseEntity
                .status(422)
                .body("Финансовая операция не выполнена");
        }
    }
} 