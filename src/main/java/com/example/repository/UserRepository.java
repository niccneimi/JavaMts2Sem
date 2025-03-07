package com.example.repository;

import org.springframework.stereotype.Repository;

import com.example.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final List<User> users = new ArrayList<>();

    public UserRepository() {
        users.add(new User(1L, "Иван", "ivan@mail.ru"));
        users.add(new User(2L, "Мария", "maria@mail.ru"));
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
}