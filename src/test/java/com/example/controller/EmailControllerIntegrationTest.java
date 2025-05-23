package com.example.controller;

import com.example.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class EmailControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private EmailService emailService;

    private WebTestClient webTestClient;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init-db.sql");

    @DynamicPropertySource
    static void registerPostgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    public void setup() {
        log.info("==== PostgreSQL container info ====");
        log.info("PostgreSQL container host: {}", postgreSQLContainer.getHost());
        log.info("PostgreSQL container port: {}", postgreSQLContainer.getFirstMappedPort());
        log.info("PostgreSQL JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
        log.info("=====================================");
        
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        
        reset(emailService);
    }

    @Test
    public void testSendEmailSync() {
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/email/send-sync")
                        .queryParam("email", "test@example.com")
                        .queryParam("subject", "Test Subject")
                        .queryParam("content", "Test Content")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Email отправлен синхронно");
    }

    @Test
    public void testSendEmailAsync() {
        when(emailService.sendEmailAsync(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(true));
        
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/email/send-async")
                        .queryParam("email", "test@example.com")
                        .queryParam("subject", "Test Subject")
                        .queryParam("content", "Test Content")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Email отправляется асинхронно");
    }

    @Test
    public void testSendEmailAsyncWait() {
        when(emailService.sendEmailAsync(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(true));
        
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/email/send-async-wait")
                        .queryParam("email", "test@example.com")
                        .queryParam("subject", "Test Subject")
                        .queryParam("content", "Test Content")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Email отправлен асинхронно");
    }

    @Test
    public void testCircuitBreakerFallback() {
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Симуляция ошибки для тестирования Circuit Breaker"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/email/send-sync")
                        .queryParam("email", "test@example.com")
                        .queryParam("subject", "Test Subject")
                        .queryParam("content", "Test Content")
                        .build())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(response -> {
                    assert response.contains("Сервис отправки email временно недоступен");
                });
    }

    @Test
    public void testRateLimiter() {
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        for (int counter = 0; counter < 10; counter++) {
            final int i = counter;
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/email/send-sync")
                            .queryParam("email", "test" + i + "@example.com")
                            .queryParam("subject", "Test Subject")
                            .queryParam("content", "Test Content")
                            .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class).isEqualTo("Email отправлен синхронно");
        }
    }

    @Test
    public void testDatabaseConnection() {
        log.info("Проверка подключения к базе данных PostgreSQL");
        assert postgreSQLContainer.isRunning() : "PostgreSQL контейнер не запущен";
        
        try {
            java.sql.Connection connection = java.sql.DriverManager.getConnection(
                postgreSQLContainer.getJdbcUrl(),
                postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword()
            );
            java.sql.Statement statement = connection.createStatement();
            java.sql.ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                log.info("Количество записей в таблице users: {}", count);
                assert count > 0 : "Таблица users пуста";
            }
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM orders");
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                log.info("Количество записей в таблице orders: {}", count);
                assert count > 0 : "Таблица orders пуста";
            }
            resultSet.close();
            statement.close();
            connection.close();
            
            log.info("Тест подключения к базе данных успешно завершен");
        } catch (Exception e) {
            log.error("Ошибка при подключении к базе данных: {}", e.getMessage());
            throw new AssertionError("Не удалось подключиться к базе данных", e);
        }
    }
} 