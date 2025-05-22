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

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

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
} 