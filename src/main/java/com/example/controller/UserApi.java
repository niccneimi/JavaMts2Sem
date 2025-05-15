package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.model.User;

import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "User API", description = "API для управления пользователями")
public interface UserApi {

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    ResponseEntity<List<User>> getAllUsers();

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному ID")
    @Parameter(name = "id", description = "ID пользователя", required = true)
    ResponseEntity<User> getUserById(Long id);

    @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя")
    @Parameter(name = "user", description = "Данные пользователя", required = true)
    ResponseEntity<User> createUser(User user);

    @Operation(summary = "Создать нескольких пользователей", description = "Создает несколько пользователей")
    @Parameter(name = "users", description = "Список пользователей", required = true)
    ResponseEntity<List<User>> createUsers(List<User> users);

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя по ID")
    @Parameter(name = "id", description = "ID пользователя", required = true)
    @Parameter(name = "user", description = "Данные пользователя", required = true)
    ResponseEntity<User> updateUser(Long id, User user);

    @Operation(summary = "Обновить email пользователя", description = "Обновляет email пользователя по ID")
    @Parameter(name = "id", description = "ID пользователя", required = true)
    @Parameter(name = "email", description = "Новый email пользователя", required = true)
    ResponseEntity<User> updateUserEmail(Long id, String email);

    @Operation(summary = "Частичное обновление пользователя", description = "Частично обновляет данные пользователя по ID")
    @Parameter(name = "id", description = "ID пользователя", required = true)
    @Parameter(name = "user", description = "Данные пользователя", required = true)
    ResponseEntity<User> partialUpdateUser(Long id, User user);

    @Operation(summary = "Частичное обновление имени пользователя", description = "Частично обновляет имя пользователя по ID")
    @Parameter(name = "id", description = "ID пользователя", required = true)
    @Parameter(name = "name", description = "Новое имя пользователя", required = true)
    ResponseEntity<User> partialUpdateUserName(Long id, String name);

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @Parameter(name = "id", description = "ID пользователя", required = true)
    ResponseEntity<Void> deleteUser(Long id);

    @Operation(summary = "Удалить нескольких пользователей", description = "Удаляет нескольких пользователей по списку ID")
    @Parameter(name = "ids", description = "Список ID пользователей", required = true)
    ResponseEntity<Void> deleteUsers(List<Long> ids);
} 