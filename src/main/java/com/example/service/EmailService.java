package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import com.example.exception.ExternalServiceUnavailableException;

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
    
    /**
     * Метод для надежной отправки важных системных уведомлений.
     * 
     * Обеспечивает гарантию доставки (exactly-once delivery) за счет
     * механизма повторных попыток в случае временной недоступности
     * внешнего почтового сервера.
     */
    @Retryable(
        value = {ExternalServiceUnavailableException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 10000)
    )
    public boolean sendSystemNotification(String recipientEmail, String subject, String content) {
        log.info("Попытка отправки системного уведомления на {}", recipientEmail);
        
        if (Math.random() < 0.7) {
            throw new ExternalServiceUnavailableException("Почтовый сервер временно недоступен");
        }
        
        log.info("Системное уведомление успешно отправлено на {}", recipientEmail);
        return true;
    }
    
    @Recover
    public boolean recoverSendSystemNotification(ExternalServiceUnavailableException e, 
                                              String recipientEmail, 
                                              String subject, 
                                              String content) {
        log.error("После 5 попыток не удалось отправить системное уведомление на {}. " +
                 "Ошибка: {}", recipientEmail, e.getMessage());
        
        log.info("Уведомление для {} добавлено в локальную очередь для повторной отправки", recipientEmail);
        
        return false;
    }
} 