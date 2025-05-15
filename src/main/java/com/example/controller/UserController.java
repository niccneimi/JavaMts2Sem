package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @Override
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PostMapping("/bulk")
    @Override
    public ResponseEntity<List<User>> createUsers(@RequestBody List<User> users) {
        return ResponseEntity.ok(userService.createUsers(users));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PutMapping("/email/{id}")
    @Override
    public ResponseEntity<User> updateUserEmail(@PathVariable Long id, @RequestBody String email) {
        return ResponseEntity.ok(userService.updateUserEmail(id, email));
    }

    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<User> partialUpdateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.partialUpdateUser(id, user));
    }

    @PatchMapping("/name/{id}")
    @Override
    public ResponseEntity<User> partialUpdateUserName(@PathVariable Long id, @RequestBody String name) {
        return ResponseEntity.ok(userService.partialUpdateUserName(id, name));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @Override
    public ResponseEntity<Void> deleteUsers(@RequestBody List<Long> ids) {
        userService.deleteUsers(ids);
        return ResponseEntity.noContent().build();
    }
} 