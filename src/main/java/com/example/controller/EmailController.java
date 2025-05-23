package com.example.controller;

import com.example.service.EmailService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RateLimiter(name = "emailService")
@CircuitBreaker(name = "emailService", fallbackMethod = "commonFallback")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/send-sync")
    public ResponseEntity<String> sendEmail(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String content) {
        
        log.info("Получен запрос на синхронную отправку email на адрес: {}", email);
        boolean result = emailService.sendEmail(email, subject, content);
        
        if (result) {
            return ResponseEntity.ok("Email отправлен синхронно");
        } else {
            return ResponseEntity.internalServerError().body("Ошибка при отправке email");
        }
    }

    @GetMapping("/send-async")
    public ResponseEntity<String> sendEmailAsync(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String content) {
        
        log.info("Получен запрос на асинхронную отправку email на адрес: {}", email);
        emailService.sendEmailAsync(email, subject, content);
        
        return ResponseEntity.ok("Email отправляется асинхронно");
    }
    
    @GetMapping("/send-async-wait")
    public CompletableFuture<ResponseEntity<String>> sendEmailAsyncAndWait(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String content) {
        
        log.info("Получен запрос на асинхронную отправку email с ожиданием на адрес: {}", email);
        return emailService.sendEmailAsync(email, subject, content)
                .thenApply(result -> {
                    if (result) {
                        return ResponseEntity.ok("Email отправлен асинхронно");
                    } else {
                        return ResponseEntity.internalServerError().body("Ошибка при отправке email");
                    }
                });
    }
    
    public ResponseEntity<String> commonFallback(Exception ex) {
        log.error("Сработал fallback для EmailController: {}", ex.getMessage());
        return ResponseEntity.status(503)
            .body("Сервис отправки email временно недоступен. Пожалуйста, повторите запрос позже.");
    }
    
    public CompletableFuture<ResponseEntity<String>> commonFallback(
            String email, String subject, String content, Exception ex) {
        log.error("Сработал fallback для асинхронной отправки email: {}", ex.getMessage());
        return CompletableFuture.completedFuture(
            ResponseEntity.status(503)
                .body("Сервис отправки email временно недоступен. Пожалуйста, повторите запрос позже.")
        );
    }
} 