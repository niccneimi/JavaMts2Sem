package com.example.controller;

import com.example.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/email")
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
        
        emailService.sendEmailAsync(email, subject, content);
        
        return ResponseEntity.ok("Email отправляется асинхронно");
    }
    
    @GetMapping("/send-async-wait")
    public CompletableFuture<ResponseEntity<String>> sendEmailAsyncAndWait(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String content) {
        
        return emailService.sendEmailAsync(email, subject, content)
                .thenApply(result -> {
                    if (result) {
                        return ResponseEntity.ok("Email отправлен асинхронно");
                    } else {
                        return ResponseEntity.internalServerError().body("Ошибка при отправке email");
                    }
                });
    }
} 