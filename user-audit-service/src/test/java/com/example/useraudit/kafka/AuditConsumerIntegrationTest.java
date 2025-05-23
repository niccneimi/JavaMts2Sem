package com.example.useraudit.kafka;

import com.example.useraudit.dto.AuditMessageDto;
import com.example.useraudit.model.UserAudit;
import com.example.useraudit.service.UserAuditService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"user-audit"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.topics.user-audit=user-audit",
        "spring.kafka.consumer.group-id=user-audit-service-test"
})
class AuditConsumerIntegrationTest {

    private static final String TOPIC = "user-audit";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private UserAuditService userAuditService;

    @Captor
    private ArgumentCaptor<UserAudit> auditCaptor;

    private Producer<String, AuditMessageDto> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        
        producer = new DefaultKafkaProducerFactory<>(
                producerProps,
                new StringSerializer(),
                new JsonSerializer<AuditMessageDto>()
        ).createProducer();
    }

    @AfterEach
    void tearDown() {
        producer.close();
    }

    @Test
    void shouldConsumeAuditMessageAndSaveToDatabase() throws Exception {
        AuditMessageDto testMessage = new AuditMessageDto();
        testMessage.setUserId(1L);
        testMessage.setUsername("testuser");
        testMessage.setActionType("CREATE");
        testMessage.setEntityType("User");
        testMessage.setEntityId("1");
        testMessage.setActionDetails("Создание пользователя");
        testMessage.setIpAddress("127.0.0.1");
        testMessage.setTimestamp(Instant.now());

        when(userAuditService.createAuditRecord(any(UserAudit.class))).thenReturn(new UserAudit());

        producer.send(new ProducerRecord<>(TOPIC, "testKey", testMessage)).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
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
        });
    }
} 