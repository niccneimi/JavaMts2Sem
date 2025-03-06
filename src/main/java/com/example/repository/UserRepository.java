package com.example.repository;

import com.example.model.User;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    private final List<User> users = new ArrayList<>();

    public UserRepository() {
        users.add(new User(1L, "Иван", "ivan@mail.ru"));
        users.add(new User(2L, "Мария", "maria@mail.ru"));
    }

    public List<User> findAll() {
        return users;
    }
}