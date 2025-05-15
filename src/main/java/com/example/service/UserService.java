package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        log.debug("Получение списка всех пользователей");
        List<User> users = userRepository.findAll().get();
        return users;
    }

    public User getUserById(Long id) {
        log.debug("Получение одного пользователя по Id");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return user;
    }

    public User createUser(User user) {
        log.debug("Создание пользователя");
        userRepository.save(user);
        return user;
    }

    public List<User> createUsers(List<User> users) {
        log.debug("Создание нескольких пользователей");
        users.forEach(userRepository::save);
        return users;
    }

    public User updateUser(Long id, User user) {
        log.debug("Обновление пользователя");
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        return existingUser;
    }

    public User updateUserEmail(Long id, String email) {
        log.debug("Обновление email пользователя");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setEmail(email);
        return user;
    }

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

    public User partialUpdateUserName(Long id, String name) {
        log.debug("Установка имени пользователя");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setName(name);
        return user;
    }

    public void deleteUser(Long id) {
        log.debug("Удаление пользователя");
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        userRepository.delete(user);
    }

    public void deleteUsers(List<Long> ids) {
        log.debug("Удаление нескольких пользователей");
        ids.forEach(this::deleteUser);
    }
} 