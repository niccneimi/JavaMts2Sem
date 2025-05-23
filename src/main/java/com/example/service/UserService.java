package com.example.service;

import com.example.dto.UserDto;
import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuditProducerService auditProducerService;
    
    private final Map<String, Boolean> processedOperations = new ConcurrentHashMap<>();
    
    public UserService(UserRepository userRepository, AuditProducerService auditProducerService) {
        this.userRepository = userRepository;
        this.auditProducerService = auditProducerService;
    }

    @Cacheable(value = "users")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<UserDto> getAllUsers() {
        log.debug("Получение списка всех пользователей");
        return userRepository.findAll().stream()
            .map(UserDto::new)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "userById", key = "#id")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public UserDto getUserById(Long id) {
        log.debug("Получение одного пользователя по Id");
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return new UserDto(user);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public UserDto getUserWithOrders(Long id) {
        log.debug("Получение пользователя с заказами по Id");
        User user = userRepository.findByIdWithOrders(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return new UserDto(user);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#user.id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public UserDto createUser(User user) {
        log.debug("Создание пользователя");
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        User savedUser = userRepository.save(user);
        
        auditProducerService.sendUserCreationAudit(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getIpAddress() != null ? savedUser.getIpAddress() : "system"
        );
        
        return new UserDto(savedUser);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public List<UserDto> createUsers(List<User> users) {
        log.debug("Создание нескольких пользователей");
        List<User> savedUsers = userRepository.saveAll(users);
        return savedUsers.stream()
            .map(UserDto::new)
            .collect(Collectors.toList());
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public UserDto updateUser(Long id, User userData) {
        log.debug("Обновление пользователя");
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        String oldEmail = existingUser.getEmail();
        String oldName = existingUser.getName();
        
        if (!existingUser.getEmail().equals(userData.getEmail()) && 
            userRepository.existsByEmail(userData.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        existingUser.setName(userData.getName());
        existingUser.setEmail(userData.getEmail());
        User savedUser = userRepository.save(existingUser);
        
        StringBuilder changedFields = new StringBuilder();
        if (!oldName.equals(savedUser.getName())) {
            changedFields.append("имя с '").append(oldName).append("' на '").append(savedUser.getName()).append("'");
        }
        if (!oldEmail.equals(savedUser.getEmail())) {
            if (changedFields.length() > 0) {
                changedFields.append(", ");
            }
            changedFields.append("email с '").append(oldEmail).append("' на '").append(savedUser.getEmail()).append("'");
        }
        
        auditProducerService.sendUserUpdateAudit(
                savedUser.getId(),
                savedUser.getName(),
                userData.getIpAddress() != null ? userData.getIpAddress() : "system",
                changedFields.toString()
        );
        
        return new UserDto(savedUser);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public UserDto updateUserEmail(Long id, String email) {
        log.debug("Обновление email пользователя");
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setEmail(email);
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public UserDto partialUpdateUser(Long id, User userData) {
        log.debug("Частичное обновление пользователя");
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (userData.getName() != null) {
            existingUser.setName(userData.getName());
        }
        
        if (userData.getEmail() != null && !userData.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userData.getEmail())) {
                throw new RuntimeException("Пользователь с таким email уже существует");
            }
            existingUser.setEmail(userData.getEmail());
        }
        
        User savedUser = userRepository.save(existingUser);
        return new UserDto(savedUser);
    }

    @Caching(
        put = {
            @CachePut(value = "userById", key = "#id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public UserDto partialUpdateUserName(Long id, String name) {
        log.debug("Установка имени пользователя");
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setName(name);
        User savedUser = userRepository.save(user);
        return new UserDto(savedUser);
    }

    @Caching(
        evict = {
            @CacheEvict(value = "userById", key = "#id"),
            @CacheEvict(value = "users", allEntries = true)
        }
    )
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void deleteUser(Long id) {
        log.debug("Удаление пользователя");
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (userRepository.hasOrders(id)) {
            throw new RuntimeException("Нельзя удалить пользователя с заказами");
        }
        
        String username = user.getName();
        
        userRepository.delete(user);
        
        auditProducerService.sendUserDeletionAudit(
                id,
                username,
                "system"
        );
    }

    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteUsers(List<Long> ids) {
        log.debug("Удаление нескольких пользователей");
        ids.forEach(this::deleteUser);
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
                throw e;
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