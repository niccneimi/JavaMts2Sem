package com.example.useraudit.service;

import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.example.useraudit.model.UserAudit;
import com.example.useraudit.repository.UserAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuditServiceNegativeTest {

    @Mock
    private UserAuditRepository userAuditRepository;

    @InjectMocks
    private UserAuditService userAuditService;

    private UserAudit testAudit;

    @BeforeEach
    void setUp() {
        testAudit = UserAudit.builder()
                .id(UUID.randomUUID())
                .userId(1L)
                .username("testuser")
                .actionType("UPDATE")
                .entityType("User")
                .entityId("100")
                .actionDetails("Changed user email from old@example.com to new@example.com")
                .ipAddress("192.168.1.1")
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void createAuditRecord_DatabaseError() {
        when(userAuditRepository.save(any(UserAudit.class)))
                .thenThrow(new MockDriverException("Ошибка соединения с базой данных"));

        assertThrows(UserAuditService.AuditServiceException.class, () -> {
            userAuditService.createAuditRecord(testAudit);
        });
    }

    @Test
    void getAuditRecordsByUserId_DatabaseError() {
        when(userAuditRepository.findByUserId(any(Long.class)))
                .thenThrow(new MockDriverException("Ошибка чтения из базы данных"));

        assertThrows(UserAuditService.AuditServiceException.class, () -> {
            userAuditService.getAuditRecordsByUserId(1L);
        });
    }

    @Test
    void getAuditRecordsByEntity_DatabaseError() {
        when(userAuditRepository.findByEntity(any(String.class), any(String.class)))
                .thenThrow(new MockDriverException("Ошибка чтения из базы данных"));

        assertThrows(UserAuditService.AuditServiceException.class, () -> {
            userAuditService.getAuditRecordsByEntity("User", "100");
        });
    }

    private static class MockDriverException extends DriverException {
        public MockDriverException(String message) {
            super(message, null, null, false);
        }
        
        @Override
        public DriverException copy() {
            return new MockDriverException(getMessage());
        }
    }
} 