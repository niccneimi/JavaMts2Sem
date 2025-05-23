package com.example.controller;

import com.example.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(emailService.sendEmailAsync(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(true));
    }

    @Test
    public void testSendEmailSync() {
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
    public void testRateLimiterAndCircuitBreaker() {
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Симуляция ошибки для тестирования Circuit Breaker"));

        for (int counter = 0; counter < 5; counter++) {
            final int i = counter;
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/email/send-sync")
                            .queryParam("email", "test" + i + "@example.com")
                            .queryParam("subject", "Test Subject")
                            .queryParam("content", "Test Content")
                            .build())
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .returnResult();
        }

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
} 