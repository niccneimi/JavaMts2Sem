package com.example.useraudit.service;

import com.datastax.oss.driver.api.core.DriverException;
import com.example.useraudit.model.UserAudit;
import com.example.useraudit.repository.UserAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuditService {

    private final UserAuditRepository userAuditRepository;

    public UserAudit createAuditRecord(UserAudit audit) {
        try {
            log.debug("Создание записи аудита: {}", audit);
            return userAuditRepository.save(audit);
        } catch (DriverException e) {
            log.error("Ошибка при сохранении записи аудита: {}", e.getMessage(), e);
            throw new AuditServiceException("Не удалось сохранить запись аудита", e);
        }
    }

    public List<UserAudit> getAuditRecordsByUserId(Long userId) {
        try {
            log.debug("Получение записей аудита для пользователя с ID: {}", userId);
            return userAuditRepository.findByUserId(userId);
        } catch (DriverException e) {
            log.error("Ошибка при получении записей аудита для пользователя {}: {}", userId, e.getMessage(), e);
            throw new AuditServiceException("Не удалось получить записи аудита для пользователя " + userId, e);
        }
    }

    public List<UserAudit> getAuditRecordsByEntity(String entityType, String entityId) {
        try {
            log.debug("Получение записей аудита для сущности типа {} с ID: {}", entityType, entityId);
            return userAuditRepository.findByEntity(entityType, entityId);
        } catch (DriverException e) {
            log.error("Ошибка при получении записей аудита для сущности {} с ID {}: {}", 
                    entityType, entityId, e.getMessage(), e);
            throw new AuditServiceException(
                    "Не удалось получить записи аудита для сущности " + entityType + " с ID " + entityId, e);
        }
    }

    public static class AuditServiceException extends RuntimeException {
        public AuditServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 