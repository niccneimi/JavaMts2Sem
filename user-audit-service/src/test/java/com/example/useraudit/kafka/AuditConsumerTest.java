package com.example.useraudit.kafka;

import com.example.useraudit.dto.AuditMessageDto;
import com.example.useraudit.model.UserAudit;
import com.example.useraudit.service.UserAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditConsumerTest {

    @Mock
    private UserAuditService userAuditService;

    @InjectMocks
    private AuditConsumer auditConsumer;

    @Captor
    private ArgumentCaptor<UserAudit> auditCaptor;

    private AuditMessageDto testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new AuditMessageDto();
        testMessage.setUserId(1L);
        testMessage.setUsername("testuser");
        testMessage.setActionType("CREATE");
        testMessage.setEntityType("User");
        testMessage.setEntityId("1");
        testMessage.setActionDetails("Создание пользователя");
        testMessage.setIpAddress("127.0.0.1");
        testMessage.setTimestamp(Instant.now());
    }

    @Test
    void consumeAuditMessage_shouldSaveAuditRecord() {
        when(userAuditService.createAuditRecord(any(UserAudit.class))).thenReturn(new UserAudit());

        auditConsumer.consumeAuditMessage(testMessage);

        verify(userAuditService, times(1)).createAuditRecord(auditCaptor.capture());
        
        UserAudit capturedAudit = auditCaptor.getValue();
        assertNotNull(capturedAudit.getId());
        assertEquals(testMessage.getUserId(), capturedAudit.getUserId());
        assertEquals(testMessage.getUsername(), capturedAudit.getUsername());
        assertEquals(testMessage.getActionType(), capturedAudit.getActionType());
        assertEquals(testMessage.getEntityType(), capturedAudit.getEntityType());
        assertEquals(testMessage.getEntityId(), capturedAudit.getEntityId());
        assertEquals(testMessage.getActionDetails(), capturedAudit.getActionDetails());
        assertEquals(testMessage.getIpAddress(), capturedAudit.getIpAddress());
        assertEquals(testMessage.getTimestamp(), capturedAudit.getTimestamp());
    }

    @Test
    void consumeAuditMessage_shouldHandleException() {
        when(userAuditService.createAuditRecord(any(UserAudit.class)))
                .thenThrow(new RuntimeException("Test exception"));

        auditConsumer.consumeAuditMessage(testMessage);

        verify(userAuditService, times(1)).createAuditRecord(any(UserAudit.class));
    }

    @Test
    void consumeAuditMessage_withNullTimestamp_shouldUseCurrentTimestamp() {
        testMessage.setTimestamp(null);
        when(userAuditService.createAuditRecord(any(UserAudit.class))).thenReturn(new UserAudit());

        auditConsumer.consumeAuditMessage(testMessage);

        verify(userAuditService, times(1)).createAuditRecord(auditCaptor.capture());
        
        UserAudit capturedAudit = auditCaptor.getValue();
        assertNotNull(capturedAudit.getId());
        assertEquals(testMessage.getUserId(), capturedAudit.getUserId());
        assertEquals(testMessage.getUsername(), capturedAudit.getUsername());
        assertNotNull(capturedAudit.getTimestamp());
    }
} 