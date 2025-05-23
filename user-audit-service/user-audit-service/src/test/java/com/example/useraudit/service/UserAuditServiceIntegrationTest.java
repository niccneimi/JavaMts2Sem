package com.example.useraudit.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.useraudit.model.UserAudit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserAuditServiceIntegrationTest {

    @Container
    private static final CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.0")
            .withExposedPorts(9042);

    @Autowired
    private UserAuditService userAuditService;

    @Autowired
    private CqlSession cqlSession;

    private UserAudit testAudit;

    @DynamicPropertySource
    static void registerCassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("cassandra.contactPoints", cassandra::getHost);
        registry.add("cassandra.port", cassandra::getFirstMappedPort);
        registry.add("cassandra.keyspace", () -> "user_audit");
        registry.add("cassandra.datacenter", () -> "datacenter1");
    }

    @BeforeEach
    void setUp() {
        cqlSession.execute("CREATE KEYSPACE IF NOT EXISTS user_audit " +
                           "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
        
        cqlSession.execute("USE user_audit");
        
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

    @AfterEach
    void tearDown() {
        cqlSession.execute("TRUNCATE user_audit");
    }

    @Test
    void createAuditRecord_Success() {
        UserAudit savedAudit = userAuditService.createAuditRecord(testAudit);
        
        assertNotNull(savedAudit);
        assertEquals(testAudit.getId(), savedAudit.getId());
        assertEquals(testAudit.getUserId(), savedAudit.getUserId());
        assertEquals(testAudit.getUsername(), savedAudit.getUsername());
        assertEquals(testAudit.getActionType(), savedAudit.getActionType());
    }

    @Test
    void createAuditRecord_WithNullId_ShouldGenerateId() {
        testAudit.setId(null);
        
        UserAudit savedAudit = userAuditService.createAuditRecord(testAudit);
        
        assertNotNull(savedAudit);
        assertNotNull(savedAudit.getId());
    }

    @Test
    void getAuditRecordsByUserId_Success() {
        userAuditService.createAuditRecord(testAudit);
        
        List<UserAudit> auditRecords = userAuditService.getAuditRecordsByUserId(testAudit.getUserId());
        
        assertNotNull(auditRecords);
        assertFalse(auditRecords.isEmpty());
        assertEquals(1, auditRecords.size());
        assertEquals(testAudit.getId(), auditRecords.get(0).getId());
    }

    @Test
    void getAuditRecordsByUserId_UserNotFound() {
        List<UserAudit> auditRecords = userAuditService.getAuditRecordsByUserId(999L);
        
        assertNotNull(auditRecords);
        assertTrue(auditRecords.isEmpty());
    }

    @Test
    void getAuditRecordsByEntity_Success() {
        userAuditService.createAuditRecord(testAudit);
        
        List<UserAudit> auditRecords = userAuditService.getAuditRecordsByEntity(
                testAudit.getEntityType(), testAudit.getEntityId());
        
        assertNotNull(auditRecords);
        assertFalse(auditRecords.isEmpty());
        assertEquals(1, auditRecords.size());
        assertEquals(testAudit.getId(), auditRecords.get(0).getId());
    }

    @Test
    void getAuditRecordsByEntity_EntityNotFound() {
        List<UserAudit> auditRecords = userAuditService.getAuditRecordsByEntity("Product", "999");
        
        assertNotNull(auditRecords);
        assertTrue(auditRecords.isEmpty());
    }
} 