package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.EmailService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/system")
    public ResponseEntity<String> sendSystemNotification(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String content) {
        
        log.info("Получен запрос на отправку системного уведомления на {}", email);
        
        try {
            boolean result = emailService.sendSystemNotification(email, subject, content);
            
            if (result) {
                return ResponseEntity.ok("Системное уведомление успешно отправлено");
            } else {
                return ResponseEntity
                    .status(500)
                    .body("Не удалось отправить системное уведомление после всех попыток");
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке системного уведомления: {}", e.getMessage());
            return ResponseEntity
                .status(500)
                .body("Произошла ошибка: " + e.getMessage());
        }
    }
} 