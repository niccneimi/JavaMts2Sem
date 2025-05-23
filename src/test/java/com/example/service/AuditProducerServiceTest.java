package com.example.service;

import com.example.dto.AuditMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditProducerServiceTest {

    private static final String TOPIC = "user-audit";

    @Mock
    private KafkaTemplate<String, AuditMessageDto> kafkaTemplate;

    @InjectMocks
    private AuditProducerService auditProducerService;

    @Captor
    private ArgumentCaptor<AuditMessageDto> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auditProducerService, "auditTopic", TOPIC);
        
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, AuditMessageDto>> future = mock(CompletableFuture.class);
        
        when(kafkaTemplate.send(eq(TOPIC), any(String.class), any(AuditMessageDto.class))).thenReturn(future);
    }

    @Test
    void sendUserCreationAudit_Success() {
        Long userId = 1L;
        String username = "testuser";
        String ipAddress = "127.0.0.1";

        auditProducerService.sendUserCreationAudit(userId, username, ipAddress);

        verify(kafkaTemplate).send(eq(TOPIC), eq(userId.toString()), messageCaptor.capture());
        
        AuditMessageDto message = messageCaptor.getValue();
        assertNotNull(message);
        assertEquals(userId, message.getUserId());
        assertEquals(username, message.getUsername());
        assertEquals(ipAddress, message.getIpAddress());
        assertEquals("CREATE", message.getActionType());
        assertEquals("User", message.getEntityType());
        assertEquals(userId.toString(), message.getEntityId());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void sendUserUpdateAudit_Success() {
        Long userId = 2L;
        String username = "updateduser";
        String ipAddress = "192.168.1.1";
        String changedFields = "email с 'old@example.com' на 'new@example.com'";

        auditProducerService.sendUserUpdateAudit(userId, username, ipAddress, changedFields);

        verify(kafkaTemplate).send(eq(TOPIC), eq(userId.toString()), messageCaptor.capture());
        
        AuditMessageDto message = messageCaptor.getValue();
        assertNotNull(message);
        assertEquals(userId, message.getUserId());
        assertEquals(username, message.getUsername());
        assertEquals(ipAddress, message.getIpAddress());
        assertEquals("UPDATE", message.getActionType());
        assertEquals("User", message.getEntityType());
        assertEquals(userId.toString(), message.getEntityId());
        assertEquals("Обновлены поля пользователя: " + changedFields, message.getActionDetails());
        assertNotNull(message.getTimestamp());
    }
} 