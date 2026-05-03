package com.example.demo.service;

import java.util.Optional;

import com.example.demo.entity.User;

public interface UserService {

    User getById(Long userId);

    User getByUsername(String username);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User save(User user);
}
