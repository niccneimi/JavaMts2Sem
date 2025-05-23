package com.example.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
public class UserRepository {
    private final List<User> users = new ArrayList<>();
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public UserRepository() {
        users.add(new User(1L, "Иван", "ivan@mail.ru"));
        users.add(new User(2L, "Мария", "maria@mail.ru"));
        this.restTemplate = new RestTemplate();
        this.webClient = WebClient.builder().build();
    }

    public Optional<List<User>> findAll() {
        return Optional.of(users);
    }

    public Optional<User> findById(Long id) {
        return users.stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst();
    }

    public void save(User user) {
        users.add(user);
    }

    public void delete(User user) {
        users.remove(user);
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserDataFromExternalApi(Long userId) {
        String apiUrl = "https://jsonplaceholder.typicode.com/users/" + userId;
        Map<String, Object> userData = restTemplate.getForObject(apiUrl, Map.class);
        
        System.out.println("Получены данные с помощью RestTemplate: " + userData);
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
        
        System.out.println("Получены задачи с помощью WebClient (блокирующий): " + todos);
        return todos;
    }
}