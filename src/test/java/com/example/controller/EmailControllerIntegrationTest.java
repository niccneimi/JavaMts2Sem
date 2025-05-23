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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EmailControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private EmailService emailService;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DataSource dataSource() {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            dataSource.setUser("sa");
            dataSource.setPassword("sa");
            return dataSource;
        }
    }
} 