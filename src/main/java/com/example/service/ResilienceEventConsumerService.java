package com.example.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ResilienceEventConsumerService {

    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                    .onSuccess(event -> log.info("Circuit Breaker: {}, успешно: {}", event.getCircuitBreakerName(), event.getElapsedDuration()))
                    .onError(event -> log.error("Circuit Breaker: {}, ошибка: {}", event.getCircuitBreakerName(), event.getThrowable().getMessage()))
                    .onStateTransition(event -> log.info("Circuit Breaker: {}, смена состояния: {} -> {}", 
                                                          event.getCircuitBreakerName(), 
                                                          event.getStateTransition().getFromState(),
                                                          event.getStateTransition().getToState()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
            }
        };
    }

    @Bean
    public RegistryEventConsumer<RateLimiter> rateLimiterEventConsumer() {
        return new RegistryEventConsumer<RateLimiter>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<RateLimiter> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher()
                    .onSuccess(event -> log.info("Rate Limiter: {}, успешно: {}", event.getRateLimiterName(), event.getCreationTime()))
                    .onFailure(event -> log.error("Rate Limiter: {}, отказ: {}", event.getRateLimiterName(), event.getCreationTime()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<RateLimiter> entryRemoveEvent) {
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<RateLimiter> entryReplacedEvent) {
            }
        };
    }
} 