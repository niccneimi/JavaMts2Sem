package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public ExternalApiService() {
        this.restTemplate = new RestTemplate();
        this.webClient = WebClient.builder().build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserDataFromExternalApi(Long userId) {
        String apiUrl = "https://jsonplaceholder.typicode.com/users/" + userId;
        Map<String, Object> userData = restTemplate.getForObject(apiUrl, Map.class);
        
        log.info("Получены данные с помощью RestTemplate: {}", userData);
        return userData;
    }
    
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUserTodosFromExternalApi(Long userId) {
        String apiUrl = "https://jsonplaceholder.typicode.com/users/" + userId + "/todos";
        
        List<Map<String, Object>> todos = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToMono(List.class)
                .block();
        
        log.info("Получены задачи с помощью WebClient (блокирующий): {}", todos);
        return todos;
    }
} 