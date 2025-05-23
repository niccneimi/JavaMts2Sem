package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    
    private final Map<String, Boolean> processedOperations = new ConcurrentHashMap<>();
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        log.debug("Получение списка всех пользователей");
        return userRepository.findAll();
    }

    @Cacheable(value = "userById", key = "#id")
    public User getUserById(Long id) {
        log.debug("Получение одного пользователя по Id");
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#user.id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    public User createUser(User user) {
        log.debug("Создание пользователя");
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public List<User> createUsers(List<User> users) {
        log.debug("Создание нескольких пользователей");
        return userRepository.saveAll(users);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    public User updateUser(Long id, User userData) {
        log.debug("Обновление пользователя");
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        existingUser.setName(userData.getName());
        existingUser.setEmail(userData.getEmail());
        return userRepository.save(existingUser);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    public User updateUserEmail(Long id, String email) {
        log.debug("Обновление email пользователя");
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    public User partialUpdateUser(Long id, User userData) {
        log.debug("Частисное обновление пользователя");
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (userData.getName() != null) {
            existingUser.setName(userData.getName());
        }
        if (userData.getEmail() != null) {
            existingUser.setEmail(userData.getEmail());
        }
        return userRepository.save(existingUser);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    public User partialUpdateUserName(Long id, String name) {
        log.debug("Установка имени пользователя");
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setName(name);
        return userRepository.save(user);
    }

    @Caching(
        evict = {
            @CacheEvict(value = "userById", key = "#id"),
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Удаление пользователя");
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        userRepository.delete(user);
    }

    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    @Transactional
    public void deleteUsers(List<Long> ids) {
        log.debug("Удаление нескольких пользователей");
        ids.forEach(this::deleteUser);
    }
    
    public boolean processFinancialOperation(String operationId, Long userId, double amount) {
        log.info("Запрос на финансовую операцию с ID: {}, для пользователя: {}, сумма: {}", 
                operationId, userId, amount);
        
        String actualOperationId = operationId != null ? operationId : UUID.randomUUID().toString();
        
        synchronized (getOperationLock(actualOperationId)) {
            if (processedOperations.containsKey(actualOperationId)) {
                log.info("Операция с ID {} уже была выполнена ранее. Повторное выполнение пропущено.", 
                        actualOperationId);
                return processedOperations.get(actualOperationId);
            }
            
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
                
                log.info("Выполняется финансовая операция для пользователя: {}", userId);
                
                boolean operationSuccessful = processPayment(user, amount);
                
                processedOperations.put(actualOperationId, operationSuccessful);
                
                log.info("Финансовая операция {} для пользователя {} выполнена успешно: {}", 
                        actualOperationId, userId, operationSuccessful);
                return operationSuccessful;
                
            } catch (Exception e) {
                log.error("Ошибка при выполнении финансовой операции: {}", e.getMessage());
                processedOperations.put(actualOperationId, false);
                return false;
            }
        }
    }
    
    private Object getOperationLock(String operationId) {
        return operationId.intern();
    }
    
    private boolean processPayment(User user, double amount) {
        log.info("Обработка платежа на сумму {} для пользователя {}", amount, user.getId());
        
        return Math.random() > 0.2;
    }
} 