package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external")
public class UserApiController {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserFromExternalApi(@PathVariable Long id) {
        Map<String, Object> userData = userRepository.getUserDataFromExternalApi(id);
        return ResponseEntity.ok(userData);
    }

    @GetMapping("/users/{id}/todos")
    public ResponseEntity<List<Map<String, Object>>> getUserTodosFromExternalApi(@PathVariable Long id) {
        List<Map<String, Object>> todos = userRepository.getUserTodosFromExternalApi(id);
        return ResponseEntity.ok(todos);
    }
} 