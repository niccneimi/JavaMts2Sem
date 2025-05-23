package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
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
        List<User> users = userRepository.findAll().get();
        return users;
    }

    @Cacheable(value = "userById", key = "#id")
    public User getUserById(Long id) {
        log.debug("Получение одного пользователя по Id");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return user;
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#user.id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    public User createUser(User user) {
        log.debug("Создание пользователя");
        userRepository.save(user);
        return user;
    }

    @CacheEvict(value = "users", allEntries = true)
    public List<User> createUsers(List<User> users) {
        log.debug("Создание нескольких пользователей");
        users.forEach(userRepository::save);
        return users;
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    public User updateUser(Long id, User user) {
        log.debug("Обновление пользователя");
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        return existingUser;
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    public User updateUserEmail(Long id, String email) {
        log.debug("Обновление email пользователя");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setEmail(email);
        return user;
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    public User partialUpdateUser(Long id, User user) {
        log.debug("Частисное обновление пользователя");
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        return existingUser;
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    public User partialUpdateUserName(Long id, String name) {
        log.debug("Установка имени пользователя");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setName(name);
        return user;
    }

    @Caching(
        evict = {
            @CacheEvict(value = "userById", key = "#id"),
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    public void deleteUser(Long id) {
        log.debug("Удаление пользователя");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        userRepository.delete(user);
    }

    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void deleteUsers(List<Long> ids) {
        log.debug("Удаление нескольких пользователей");
        ids.forEach(this::deleteUser);
    }

    /**
     * Обеспечивает гарантию однократного исполнения за счет:
     * 1. Использования уникальных идентификаторов операций
     * 2. Отслеживания состояния выполненных операций в локальном хранилище
     * 3. Механизма блокировок для обеспечения атомарности при параллельном доступе
     * 4. Проверки статуса операции перед выполнением
     */
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
    
    // Имитация обработки платежа
    private boolean processPayment(User user, double amount) {
        log.info("Обработка платежа на сумму {} для пользователя {}", amount, user.getId());
        return Math.random() > 0.2;
    }
} 