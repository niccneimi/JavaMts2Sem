package com.example.service;

import com.example.dto.AuditMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditProducerService {

    private final KafkaTemplate<String, AuditMessageDto> kafkaTemplate;
    
    @Value("${spring.kafka.topics.user-audit:user-audit}")
    private String auditTopic;
    
    public void sendUserCreationAudit(Long userId, String username, String ipAddress) {
        sendAuditMessage(
                AuditMessageDto.builder()
                        .userId(userId)
                        .username(username)
                        .actionType("CREATE")
                        .entityType("User")
                        .entityId(userId.toString())
                        .actionDetails("Создан новый пользователь: " + username)
                        .ipAddress(ipAddress)
                        .timestamp(Instant.now())
                        .build()
        );
    }
    
    public void sendUserUpdateAudit(Long userId, String username, String ipAddress, String changedFields) {
        sendAuditMessage(
                AuditMessageDto.builder()
                        .userId(userId)
                        .username(username)
                        .actionType("UPDATE")
                        .entityType("User")
                        .entityId(userId.toString())
                        .actionDetails("Обновлены поля пользователя: " + changedFields)
                        .ipAddress(ipAddress)
                        .timestamp(Instant.now())
                        .build()
        );
    }
    
    public void sendUserDeletionAudit(Long userId, String username, String ipAddress) {
        sendAuditMessage(
                AuditMessageDto.builder()
                        .userId(userId)
                        .username(username)
                        .actionType("DELETE")
                        .entityType("User")
                        .entityId(userId.toString())
                        .actionDetails("Удален пользователь: " + username)
                        .ipAddress(ipAddress)
                        .timestamp(Instant.now())
                        .build()
        );
    }
    
    public void sendOrderCreationAudit(Long userId, String username, Long orderId, String orderDetails, String ipAddress) {
        sendAuditMessage(
                AuditMessageDto.builder()
                        .userId(userId)
                        .username(username)
                        .actionType("CREATE")
                        .entityType("Order")
                        .entityId(orderId.toString())
                        .actionDetails("Создан новый заказ: " + orderDetails)
                        .ipAddress(ipAddress)
                        .timestamp(Instant.now())
                        .build()
        );
    }
    
    public void sendOrderStatusUpdateAudit(Long userId, String username, Long orderId, String oldStatus, String newStatus, String ipAddress) {
        sendAuditMessage(
                AuditMessageDto.builder()
                        .userId(userId)
                        .username(username)
                        .actionType("UPDATE")
                        .entityType("Order")
                        .entityId(orderId.toString())
                        .actionDetails("Изменен статус заказа с '" + oldStatus + "' на '" + newStatus + "'")
                        .ipAddress(ipAddress)
                        .timestamp(Instant.now())
                        .build()
        );
    }
    
    public void sendAuditMessage(AuditMessageDto auditMessage) {
        try {
            CompletableFuture<SendResult<String, AuditMessageDto>> future = 
                    kafkaTemplate.send(auditTopic, auditMessage.getEntityId(), auditMessage);
            
            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Не удалось отправить аудит-сообщение: {}", exception.getMessage(), exception);
                } else {
                    log.debug("Аудит-сообщение успешно отправлено: topic={}, partition={}, offset={}",
                              result.getRecordMetadata().topic(),
                              result.getRecordMetadata().partition(),
                              result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке аудит-сообщения: {}", e.getMessage(), e);
        }
    }
} 