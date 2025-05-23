package com.example.useraudit.kafka;

import com.example.useraudit.dto.AuditMessageDto;
import com.example.useraudit.model.UserAudit;
import com.example.useraudit.service.UserAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditConsumer {

    private final UserAuditService userAuditService;

    @KafkaListener(topics = "${spring.kafka.topics.user-audit:user-audit}", groupId = "${spring.kafka.consumer.group-id:user-audit-service}")
    public void consumeAuditMessage(AuditMessageDto message) {
        log.info("Получено сообщение аудита: {}", message);
        
        try {
            UserAudit audit = convertToUserAudit(message);
            userAuditService.createAuditRecord(audit);
            log.info("Аудит успешно сохранен в базе данных");
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения аудита: {}", e.getMessage(), e);
        }
    }
    
    private UserAudit convertToUserAudit(AuditMessageDto message) {
        return UserAudit.builder()
                .id(UUID.randomUUID())
                .userId(message.getUserId())
                .username(message.getUsername())
                .actionType(message.getActionType())
                .entityType(message.getEntityType())
                .entityId(message.getEntityId())
                .actionDetails(message.getActionDetails())
                .ipAddress(message.getIpAddress())
                .timestamp(message.getTimestamp() != null ? message.getTimestamp() : Instant.now())
                .build();
    }
} 