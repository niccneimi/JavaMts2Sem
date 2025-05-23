package com.example.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/external")
@CircuitBreaker(name = "userApiService", fallbackMethod = "fallbackMethod")
public class UserApiController {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserFromExternalApi(@PathVariable Long id) {
        log.info("Получение данных пользователя с ID: {} из внешнего API", id);
        Map<String, Object> userData = userRepository.getUserDataFromExternalApi(id);
        return ResponseEntity.ok(userData);
    }

    @GetMapping("/users/{id}/todos")
    public ResponseEntity<List<Map<String, Object>>> getUserTodosFromExternalApi(@PathVariable Long id) {
        log.info("Получение задач пользователя с ID: {} из внешнего API", id);
        List<Map<String, Object>> todos = userRepository.getUserTodosFromExternalApi(id);
        return ResponseEntity.ok(todos);
    }
    
    public ResponseEntity<Map<String, Object>> fallbackMethod(Long id, Exception ex) {
        log.error("Сработал Circuit Breaker для запроса данных пользователя с ID: {}, ошибка: {}", id, ex.getMessage());
        return ResponseEntity.ok(Collections.singletonMap("message", "Временно недоступно. Попробуйте позже."));
    }
    
    public ResponseEntity<List<Map<String, Object>>> fallbackMethod(Long id, List<Map<String, Object>> defaultValue, Exception ex) {
        log.error("Сработал Circuit Breaker для запроса задач пользователя с ID: {}, ошибка: {}", id, ex.getMessage());
        return ResponseEntity.ok(Collections.emptyList());
    }
} 