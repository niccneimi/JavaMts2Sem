package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailService {

    @Async
    public CompletableFuture<Boolean> sendEmailAsync(String email, String subject, String content) {
        log.info("Начата асинхронная отправка email на адрес: {}", email);
        
        try {
            Thread.sleep(3000);
            log.info("Email отправлен успешно на адрес: {}", email);
            return CompletableFuture.completedFuture(true);
        } catch (InterruptedException e) {
            log.error("Ошибка при отправке email: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(false);
        }
    }
    
    public boolean sendEmail(String email, String subject, String content) {
        log.info("Начата отправка email на адрес: {}", email);
        
        try {
            Thread.sleep(3000);
            log.info("Email отправлен успешно на адрес: {}", email);
            return true;
        } catch (InterruptedException e) {
            log.error("Ошибка при отправке email: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
} 